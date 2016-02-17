'use strict';

var path    = require('path');
var webpack = require('webpack');

var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  devtool: 'source-map',

  entry: [
    'webpack/hot/only-dev-server',
    'webpack-dev-server/client?http://localhost:8080',
    path.resolve(__dirname, 'app/index.js')
  ],

  output: {
    path: path.resolve(__dirname, 'build'),
    filename: 'app.js'
  },

  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        loader: 'react-hot!babel',
        exclude: /node_modules/
      },
      {
        test: /\.(html|ico)$/,
        loader: 'file?name=[name].[ext]'
      },
      {
        test: /\.less$/,
        loader: ExtractTextPlugin.extract('css?sourceMap!less?sourceMap')
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract('css?sourceMap')
      },
      {
        test: /\.(png|jpg|eot|ttf|svg|woff|woff2)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader?limit=8192'
      }
    ]
  },

  plugins: [
    new webpack.NoErrorsPlugin(),
    new webpack.ProvidePlugin({ $: 'jquery', jQuery: 'jquery' }),
    new ExtractTextPlugin('styles.css')
  ]
}
