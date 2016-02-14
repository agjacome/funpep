'use strict';

var path    = require('path');
var webpack = require('webpack');

// TODO: split app and vendors, optimize
module.exports = {
  entry: path.resolve(__dirname, 'app/index.js'),

  output: {
    path: path.resolve(__dirname, 'build'),
    filename: 'app.js',
  },

  module: {
    loaders: [
      { test: /\.jsx?$/, loader: 'babel', exclude: /node_modules/ },
      { test: /\.html$/, loader: 'file?name=[name].[ext]' },
      { test: /\.less$/, loader: 'style!css!less'         },
      { test: /\.css$/ , loader: 'style!css'              },
      { test: /\.(png|jpg|eot|ttf|svg|woff|woff2)(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader?limit=8192' },
    ]
  },

  plugins: [
    new webpack.ProvidePlugin({ $: 'jquery', jQuery: 'jquery' })
  ]
}
