const express = require('express')
const { createClient } = require('@supabase/supabase-js')
require('dotenv').config({ path: '../.env' }) 

const app = express()
app.use(express.json())

const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
)

app.get('/feed', async (req, res) => {
  const { data, error } = await supabase
    .from('articles')
    .select('*')
    .order('published_at', { ascending: false })  // descending orders
    .limit(10)
  res.json(data)
})

// lauch server on port 3000
app.listen(3000, () => console.log('Backend running on localhost:3000'))