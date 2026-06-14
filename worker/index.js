// Load env FIRST: summarize.js reads GEMINI_API_KEY at module load, so dotenv
// must run before that require.
require('dotenv').config({ path: '../.env' })
const { createClient } = require('@supabase/supabase-js')
const { generateSummary } = require('./summarize')

const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
)

async function fetchAndStore() {
  console.log('Fetching articles from Guardian...')

  // fetch news with the Guardian API
  const response = await fetch(
    `https://content.guardianapis.com/search?api-key=${process.env.NEWS_API_KEY}&show-fields=thumbnail,bodyText&page-size=10`
  )
  const json = await response.json()
  const articles = json.response.results

  console.log(`Fetched ${articles.length} articles`)

  // store into our supabase
  for (const article of articles) {
    // Generate the summary before saving, so it's stored as part of the article.
    const summary = await generateSummary(article.fields?.bodyText)

    const { error } = await supabase
      .from('articles')
      .upsert({
        web_title: article.webTitle,
        web_url: article.webUrl,
        published_at: article.webPublicationDate,
        image_url: article.fields?.thumbnail,
        summary: summary,
        raw_data: article,
        section: article.sectionName,
      }, { onConflict: 'web_url' })

    if (error) console.error('Error inserting:', error.message)
    else console.log('Saved:', article.webTitle)
  }

  console.log('Done!')
}

fetchAndStore().catch(err => console.error('Fatal error:', err))