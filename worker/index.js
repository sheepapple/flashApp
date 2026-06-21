// Load env FIRST: summarize.js reads GEMINI_API_KEY at module load, so dotenv
// must run before that require.
require('dotenv').config({ path: '../.env' })
const { createClient } = require('@supabase/supabase-js')
const { generateSummary } = require('./summarize')

const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
)

// ---- Rate limits ----------------------------------------------------------
// Two separate APIs, two separate quotas:
//
//   Guardian (article fetch):  1 call/second, 500 calls/day
//   Gemini   (summaries):      15 calls/minute, 500 calls/day
//
// We make one Gemini call per NEW article, so the run is capped at the Gemini
// daily quota (500). Guardian pagination is cheap (~10 calls) but we still space
// it out to respect the 1-call/second limit. Articles already in the DB are
// skipped before summarizing so re-runs don't burn the daily Gemini quota on
// stories we already have.
const TARGET_ARTICLES = 500           // daily cap (== Gemini calls/day)
const PAGE_SIZE = 50                  // Guardian caps page-size at 50
const GUARDIAN_DELAY_MS = 1100        // < 1 call/sec
const SUMMARY_DELAY_MS = 4200         // ~14.3 calls/min, safely under 15/min

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

// Fetch the web_urls already stored so we don't spend summary quota on dupes.
async function getExistingUrls() {
  const urls = new Set()
  const pageSize = 1000
  for (let from = 0; ; from += pageSize) {
    const { data, error } = await supabase
      .from('articles')
      .select('web_url')
      .range(from, from + pageSize - 1)
    if (error) {
      console.error('Could not read existing articles:', error.message)
      break
    }
    if (!data || data.length === 0) break
    data.forEach((row) => urls.add(row.web_url))
    if (data.length < pageSize) break
  }
  return urls
}

async function fetchAndStore() {
  console.log(`Fetching up to ${TARGET_ARTICLES} articles from Guardian...`)

  // Pull candidate articles, paginating to reach the target. Newest-first
  // across all sections gives a wide spread of topics.
  const totalPages = Math.ceil(TARGET_ARTICLES / PAGE_SIZE)
  const articles = []
  for (let page = 1; page <= totalPages; page++) {
    const response = await fetch(
      `https://content.guardianapis.com/search?api-key=${process.env.NEWS_API_KEY}&show-fields=thumbnail,bodyText&order-by=newest&page-size=${PAGE_SIZE}&page=${page}`
    )
    const json = await response.json()
    const results = json.response?.results
    if (!results || results.length === 0) break
    articles.push(...results)
    console.log(`Fetched page ${page}/${totalPages} (${articles.length} so far)`)

    // Respect Guardian's 1-call/second limit.
    if (page < totalPages) await sleep(GUARDIAN_DELAY_MS)
  }
  console.log(`Fetched ${articles.length} articles total`)

  // Skip articles we already have so summary quota only goes to new stories.
  const existingUrls = await getExistingUrls()
  const newArticles = articles.filter((a) => !existingUrls.has(a.webUrl))
  console.log(
    `${newArticles.length} new, ${articles.length - newArticles.length} already in DB (skipped)`
  )

  // store into our supabase
  let saved = 0
  for (const article of newArticles) {
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
    else {
      saved++
      console.log(`Saved (${saved}/${newArticles.length}):`, article.webTitle)
    }

    // Throttle to stay under Gemini's 15-calls/minute limit.
    await sleep(SUMMARY_DELAY_MS)
  }

  console.log(`Done! Saved ${saved} new articles.`)
}

fetchAndStore().catch(err => console.error('Fatal error:', err))
