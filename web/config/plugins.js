const CleanWebpackPlugin = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackHarddiskPlugin = require('html-webpack-harddisk-plugin');

const path = require('path');

const rootPath = path.resolve(__dirname, './../');
const webpack = require('webpack');
const fs = require('fs');

const Validate = require('./validate');

Validate('ORIGIN');

// the path(s) that should be cleaned
const pathsToClean = [
  'public',
];

// the clean options to use
const cleanOptions = {
  root: path.resolve(rootPath),
  verbose: true,
};

const templateChunks = {
  'index.html': ['vendor', 'index'],
  'account.html': ['vendor', 'account'],
};

const templatesSrcDir = path.resolve(rootPath, 'html/');
const templateDistDir = path.resolve(rootPath, 'public/');

const isDirectory = path => fs.statSync(path).isDirectory();
const isFile = path => fs.statSync(path).isFile();

const getDirectories = p => fs.readdirSync(p).map(name => path.resolve(p, name)).filter(isDirectory);
const getFiles = p => fs.readdirSync(p).map(name => path.resolve(p, name)).filter(isFile);

const getFilesRecursively = (p) => {
  const dirs = getDirectories(p);
  const files = dirs
    .map(dir => getFilesRecursively(dir)) // go through each directory
    .reduce((a, b) => a.concat(b), []); // map returns a 2d array (array of file arrays) so flatten
  return files.concat(getFiles(p));
};

const htmlWebpackPlugins = getFilesRecursively(templatesSrcDir).map((template) => {
  let relPath = template.replace(path.resolve(templatesSrcDir, ''), '');
  relPath = relPath.replace('/', '');
  return new HtmlWebpackPlugin({
    favicon: path.resolve(rootPath, 'html/image/favicon.ico'),
    inject: true,
    template,
    filename: template.replace(templatesSrcDir, templateDistDir),
    chunks: relPath in templateChunks ? templateChunks[relPath] : [],
    alwaysWriteToDisk: true,
  });
});

let plugins = [
  new CleanWebpackPlugin(pathsToClean, cleanOptions),
  new webpack.ProvidePlugin({
    $: 'jquery',
    jQuery: 'jquery',
  }),
  new webpack.DefinePlugin({
    ORIGIN: JSON.stringify(process.env.ORIGIN),
  }),
];
plugins = [
  ...plugins,
  ...htmlWebpackPlugins,
  new HtmlWebpackHarddiskPlugin(),
];

module.exports = plugins;
