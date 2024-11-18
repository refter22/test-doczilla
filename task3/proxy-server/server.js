const express = require('express');
const cors = require('cors');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();

app.use(cors());

app.use((req, res, next) => {
    console.log('Incoming request:', {
        method: req.method,
        url: req.url
    });
    next();
});

const proxy = createProxyMiddleware({
    target: 'https://todo.doczilla.pro',
    changeOrigin: true,
    secure: false,
    pathRewrite: {
        '^/api': '/api'
    },
    onProxyReq: (proxyReq, req, res) => {
        const finalUrl = `${proxyReq.protocol}//${proxyReq.host}${proxyReq.path}`;
        console.log('Proxying to:', finalUrl);
    },
    onProxyRes: (proxyRes, req, res) => {
        console.log('Response status:', proxyRes.statusCode);
    },
    onError: (err, req, res) => {
        console.error('Proxy error:', err);
        res.status(500).send('Proxy Error: ' + err.message);
    }
});

app.use('/', proxy);

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Proxy server running on http://localhost:${PORT}`);
});