'use strict';

var path    = require('path');
var webpack = require('webpack');

var ExtractTextPlugin = require('extract-text-webpack-plugin');

// TODO: split app and vendors, optimize
module.exports = {
  devtool: 'source-map',

  entry: path.resolve(__dirname, 'app/index.js'),

  output: {
    path: path.resolve(__dirname, 'build'),
    filename: 'app.js',
  },

  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        loader: 'babel',
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
        loader: 'url-loader?limit=2500'
      }
    ]
  },

  plugins: [
    new webpack.ProvidePlugin({ $: 'jquery', jQuery: 'jquery' }),
    new ExtractTextPlugin('styles.css')
  ]
}
