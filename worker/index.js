const { createClient } = require('@supabase/supabase-js')
require('dotenv').config({ path: '../.env' })

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
    const { error } = await supabase
      .from('articles')
      .upsert({
        web_title: article.webTitle,
        web_url: article.webUrl,
        published_at: article.webPublicationDate,
        image_url: article.fields?.thumbnail,
        raw_data: article
      }, { onConflict: 'web_url' })

    if (error) console.error('Error inserting:', error.message)
    else console.log('Saved:', article.webTitle)
  }

  console.log('Done!')
}

fetchAndStore()