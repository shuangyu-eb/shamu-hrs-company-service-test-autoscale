const path = require('path');

const rootPath = path.resolve(__dirname, './../');
const srcPath = path.resolve(rootPath, 'javascript');

const envConfigPath = process.env.DOTENV_CONFIG_PATH || './config/local.env';

require('dotenv').config({
  path: path.resolve(rootPath, envConfigPath),
});

const plugins = require('./plugins.js');

module.exports = {
  mode: 'development',
  entry: {
    index: path.resolve(srcPath, 'entries/index.jsx'),
    admin: path.resolve(srcPath, 'entries/admin.jsx'),
  },
  output: {
    path: path.resolve(rootPath, 'public'),
    filename: 'assets/[name].js',
    publicPath: '/',
  },

  plugins,
  optimization: {
    splitChunks: {
      cacheGroups: {
        vendor: {
          test: /node_module/,
          chunks: 'initial',
          name: 'vendor',
          minChunks: 5,
          maxInitialRequests: 5,
          minSize: 0,
        },
      },
    },
  },
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        exclude: /(node_modules)/,
        use: [
          {
            loader: 'babel-loader',
          },
        ],
      },
      {
        test: /\.html/,
        use: {
          loader: 'html-loader',
        },
      },
      {
        test: /\.(png|jpg|svg|ico)$/,
        use: {
          loader: 'url-loader?limit=8192&name=assets/images/[hash:8][name].[ext]',
        },
      },
      {
        test: /\.s?css$/,
        use: [
          {
            loader: 'style-loader',
          },
          {
            loader: 'css-loader',
          }, {
            loader: 'postcss-loader',
            options: {
              plugins() {
                return [
                  require('precss'),
                  require('autoprefixer'),
                ];
              },
            },
          }, {
            loader: 'sass-loader', // compiles Sass to CSS
          },
        ],
      },
    ],
  },
  resolve: {
    modules: [srcPath, 'node_modules'],
    extensions: ['.js', '.jsx', '.css', '.scss', '.png', '.svg', '.jpeg', 'jpg'],
  },

  watchOptions: { // for watch mode
    ignored: /node_modules/,
  },
  devServer: {
    port: 3000,
    contentBase: path.resolve(rootPath, 'public'),
    watchOptions: {
      ignored: /node_modules/,
    },
    stats: {
      errorDetails: true,
    },
  },
};
