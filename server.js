// Clean URL Server for Royal Pearl Hotel
// Run with: node server.js
// Then open http://localhost:55593/

import http from 'http';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 55593;

// Map clean URLs to actual HTML files
const routes = {
  '/': 'home.html',
  '/home': 'home.html',
  '/rooms': 'rooms.html',
  '/restaurant': 'restaurant.html',
  '/gallery': 'gallery.html',
  '/about': 'about.html',
  '/contact': 'contact.html',
  '/booking': 'booking.html',
  '/admin': 'admin.html',
  '/admin-login': 'admin-login.html',
  '/profile': 'profile.html',
};

const server = http.createServer((req, res) => {
  let urlPath = req.url.split('?')[0]; // Remove query string
  
  // Remove trailing slash unless it's just "/"
  if (urlPath !== '/' && urlPath.endsWith('/')) {
    urlPath = urlPath.slice(0, -1);
  }
  
  // Check if it's a known clean route
  let fileName = routes[urlPath];
  
  // If not a known route, try to find the file
  if (!fileName) {
    // Check if it matches any known page without .html
    const cleanUrls = Object.keys(routes);
    for (const route of cleanUrls) {
      if (urlPath === route || urlPath === route + '.html') {
        fileName = routes[route];
        break;
      }
    }
    
    // Also try adding .html extension directly
    if (!fileName && !urlPath.includes('.')) {
      const htmlFile = urlPath.slice(1) + '.html';
      if (fs.existsSync(path.join(__dirname, htmlFile))) {
        fileName = htmlFile;
      }
    }
  }
  
  // Fallback to home for root
  if (!fileName) {
    fileName = 'home.html';
  }
  
  const filePath = path.join(__dirname, fileName);
  
  // Set content type
  const ext = path.extname(filePath);
  const contentTypes = {
    '.html': 'text/html',
    '.css': 'text/css',
    '.js': 'application/javascript',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
  };
  
  const contentType = contentTypes[ext] || 'text/plain';
  
  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === 'ENOENT') {
        // File not found, serve home page
        fs.readFile(path.join(__dirname, 'home.html'), (err2, homeContent) => {
          if (err2) {
            res.writeHead(404);
            res.end('404 Not Found');
          } else {
            res.writeHead(200, { 'Content-Type': 'text/html' });
            res.end(homeContent);
          }
        });
      } else {
        res.writeHead(500);
        res.end('Server Error');
      }
    } else {
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content);
    }
  });
});

server.listen(PORT, () => {
  console.log(`
╔════════════════════════════════════════════════════════════╗
║         Royal Pearl Hotel - Server Running                  ║
╠════════════════════════════════════════════════════════════╣
║                                                             ║
║   🌐 Open: http://localhost:${PORT}                          ║
║                                                             ║
║   Clean URLs:                                               ║
║   ├── http://localhost:${PORT}/                              ║
║   ├── http://localhost:${PORT}/rooms                        ║
║   ├── http://localhost:${PORT}/restaurant                   ║
║   ├── http://localhost:${PORT}/gallery                      ║
║   ├── http://localhost:${PORT}/about                        ║
║   ├── http://localhost:${PORT}/contact                      ║
║   ├── http://localhost:${PORT}/booking                      ║
║   ├── http://localhost:${PORT}/admin                        ║
║   └── http://localhost:${PORT}/profile                      ║
║                                                             ║
║   Press Ctrl+C to stop the server                           ║
║                                                             ║
╚════════════════════════════════════════════════════════════╝
  `);
});