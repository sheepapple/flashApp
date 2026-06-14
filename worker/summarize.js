const fs = require('fs')
const path = require('path')
const { GoogleGenAI } = require('@google/genai')

// The model-facing spec/PRD. Loaded once at startup and sent to Gemini with every
// article so summaries stay consistent. Tune behavior by editing summary-spec.md —
// no code change required.
const SUMMARY_SPEC = fs.readFileSync(path.join(__dirname, 'summary-spec.md'), 'utf8')

// One shared Gemini client for the worker.
const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY })

/**
 * generateSummary(article) — STUB (implementation to come).
 *
 * Runs at ingestion time (after the Guardian fetch, before the Supabase upsert),
 * so each article is summarized exactly once and the result is stored as part of
 * the article row.
 *
 * Will call the Gemini API using SUMMARY_SPEC as the instructions plus the
 * article's body text (article.fields.bodyText) as input. Returns null when the
 * body is missing or the API call fails, so ingestion never breaks over a summary.
 *
 * @param {string} articleText - the body text of the article to summarize
 * @returns {Promise<string|null>} the summary text, or null if it can't be produced
 */
async function generateSummary(articleText) {
  if (!articleText) return null

  try {
    const response = await ai.models.generateContent({
      model: 'gemini-3.1-flash-lite',
      contents: SUMMARY_SPEC + '\n\n' + articleText,
      config: { maxOutputTokens: 300 },
    })
    return response.text?.trim() ?? null
  } catch (err) {
    console.error('Gemini request failed:', err.message)
    return null
  }
}

module.exports = { generateSummary }