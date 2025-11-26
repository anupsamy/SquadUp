import axios from 'axios';
import { Request, Response } from 'express';

const NEWS_API_KEY = process.env.NEWS_API_KEY;
if (!NEWS_API_KEY) {
  throw new Error('NEWS_API_KEY environment variable is required');
}

export class NewsController {
  // Expects req.body.hobbies = ["Reading", "Coding", "Cooking"]
  async getNewsByHobbies(req: Request, res: Response) {
    try {
      const hobbies = req.body.hobbies;
      if (!Array.isArray(hobbies) || hobbies.length === 0) {
        return res.status(400).json({ message: 'Hobbies array is required' });
      }
      // Validate that all items are strings
      if (!hobbies.every((hobby): hobby is string => typeof hobby === 'string')) {
        return res.status(400).json({ message: 'All hobbies must be strings' });
      }

      // Fetch news for all hobbies concurrently
      const results = await Promise.all(
        hobbies.map(async (hobby) => {
          const response = await axios.get('https://newsapi.org/v2/everything', {
            params: {
              q: hobby,
              apiKey: NEWS_API_KEY,
              language: 'en',
              pageSize: 5,
            },
          });

          interface NewsArticle {
            title?: string;
            url?: string;
            source?: { name?: string };
            publishedAt?: string;
          }
          const articles = (response.data.articles as NewsArticle[]).map((a) => ({
            title: a.title ?? '',
            url: a.url ?? '',
            source: a.source?.name ?? '',
            publishedAt: a.publishedAt ?? '',
          }));

          return {
            hobby,
            articles,
          };
        })
      );

      res.json({ results });
    } catch (error) {
      console.error('NewsAPI error:', error);
      res.status(500).json({ message: 'Failed to fetch news' });
    }
  }
}
