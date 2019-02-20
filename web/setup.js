const express = require('express');
const app = express();
const path = require('path');

const htmlPath = path.join('public');

var consul = require('consul')();
const service = {
  name: 'frontend',
  id: 'frontend',
  address: '127.0.0.1',
  port: 3000
};
consul.agent.service.deregister(service, function(err) {
  if (err) throw err;
});
consul.agent.service.register(service, function(err) {
  if (err) throw err;
});

var check = {
  name: 'service ' + service.name + ' check',
  serviceid: service.id,
  http: 'http://localhost:3000/health',
  interval: '10s',
  timeout: '1s',
  notes: 'This is the health check.',
};

consul.agent.check.register(check, function(err) {
  if (err) throw err;
});

app.get('/health', function (req, res) {
  const out = { status: 'UP' };
  res.send(out);
});

app.get('/[a-zA-Z]+', function (req, res) {
  console.log(req.url);
  const fileName = req.url.substr(1) + '.html';
  res.sendFile(path.resolve(htmlPath,fileName));
});
app.get('/', function (req, res) {
  res.sendFile(path.resolve(htmlPath, 'index.html'));
});


app.use(express.static('public'));

app.listen(3000);

// consul.agent.service.deregister(service, function(err) {
//   if (err) throw err;
// });
