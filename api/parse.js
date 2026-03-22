$content = @'
export default async function handler(req, res) {
  if (req.method !== 'POST') return res.status(405).end();

  // Debug: check env var
  if (!process.env.GROK_API_KEY) {
    return res.status(500).json({ error: 'GROK_API_KEY is not configured on Vercel' });
  }

  let body = req.body;
  if (typeof body === 'string') {
    try { body = JSON.parse(body); } catch { return res.status(400).json({ error: 'Invalid JSON' }); }
  }
  const text = body?.text;
  if (!text) return res.status(400).json({ error: 'No text provided' });

  const prompt = `You are a CV parser. Extract all information from the following CV text and return it as a single JSON object. Return ONLY the JSON, no markdown, no backticks.
{"profile":{"firstName":"","lastName":"","email":"","phone":"","title":"","organization":"","location":"","bio":"","skills":"","linkedIn":"","website":"","orcidId":""},"education":[{"degree":"","institution":"","year":"","field":"","grade":""}],"projects":[{"title":"","role":"","duration":"","description":"","tags":"","fundingAgency":"","fundingAmount":"","outcomes":"","githubUrl":"","projectUrl":""}],"publications":[{"title":"","authors":"","journal":"","year":"","type":"JOURNAL","doi":"","impactFactor":"","citations":"","abstract_text":""}],"awards":[{"name":"","awardingBody":"","year":"","description":"","category":""}],"grants":[{"title":"","agency":"","amount":"","period":"","role":"","status":"Completed"}],"achievements":[{"title":"","category":"OTHER","description":"","year":""}]}
Rules: skills=comma-separated. Return [] if no items. type: JOURNAL/CONFERENCE/BOOK_CHAPTER/BOOK/PREPRINT/PATENT/THESIS/REPORT.
CV TEXT:\n${text}`;
  if (!process.env.GROK_API_KEY) {
  return res.status(500).json({ error: 'GROK_API_KEY is not set' });
  }
  try {
    const response = await fetch('https://api.x.ai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${process.env.GROK_API_KEY}`
      },
      body: JSON.stringify({
        model: 'grok-3',
        messages: [{ role: 'user', content: prompt }],
        temperature: 0.1,
        max_tokens: 4096
      })
    });
    const data = await response.json();
    if (!response.ok) {
      return res.status(response.status).json({ error: data.error?.message || 'API error' });
    }
    const result = data.choices[0].message.content;
    res.status(200).json({ candidates: [{ content: { parts: [{ text: result }] } }] });
  } catch (err) {
    res.status(500).json({ error: err.message, stack: err.stack });
  }
}
'@
Set-Content api\parse.js $content
Set-Content parse.js $content
Write-Host "Done! Now commit and push to GitHub."
