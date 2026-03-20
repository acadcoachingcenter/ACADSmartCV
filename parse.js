export default async function handler(req, res) {
  if (req.method !== 'POST') return res.status(405).end();

  const { text } = req.body;
  if (!text) return res.status(400).json({ error: 'No text provided' });

  const prompt = `You are a CV parser. Extract all information from the following CV text and return it as a single JSON object. Return ONLY the JSON, no markdown, no backticks.
{"profile":{"firstName":"","lastName":"","email":"","phone":"","title":"","organization":"","location":"","bio":"","skills":"","linkedIn":"","website":"","orcidId":""},"education":[{"degree":"","institution":"","year":"","field":"","grade":""}],"projects":[{"title":"","role":"","duration":"","description":"","tags":"","fundingAgency":"","fundingAmount":"","outcomes":"","githubUrl":"","projectUrl":""}],"publications":[{"title":"","authors":"","journal":"","year":"","type":"JOURNAL","doi":"","impactFactor":"","citations":"","abstract_text":""}],"awards":[{"name":"","awardingBody":"","year":"","description":"","category":""}],"grants":[{"title":"","agency":"","amount":"","period":"","role":"","status":"Completed"}],"achievements":[{"title":"","category":"OTHER","description":"","year":""}]}
Rules: skills=comma-separated. Return [] if no items. type: JOURNAL/CONFERENCE/BOOK_CHAPTER/BOOK/PREPRINT/PATENT/THESIS/REPORT.
CV TEXT:\n${text}`;

  try {
    const response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${process.env.GEMINI_API_KEY}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }],
          generationConfig: { temperature: 0.1, maxOutputTokens: 4096 }
        })
      }
    );

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json({ error: data.error?.message || 'API error' });
    }

    res.status(200).json(data);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
}
