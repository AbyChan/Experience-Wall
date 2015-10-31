var webpack = require('webpack');
var path = require('path');
var node_modules = path.resolve(__dirname, 'node_modules');

var jQuery = require('jquery');

var LiveReloadPlugin = require('webpack-livereload-plugin');

var config = {
    entry: ['./js/main.js'],
    output: {
        //path: path.resolve(__dirname, 'build'),
        path: __dirname + '/build',
        publicPath: '/assets/',
        contentBase: 'assets/',
        filename: 'mainBundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.css$/, // Only .css files
                loader: 'style!css' // Run both loaders
            },
            {
                test: /\.scss$/,
                loaders: ["style", "css", "sass"]
            }
        ]
    },
    plugins: [
        new LiveReloadPlugin(),
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
            "window.jQuery": 'jquery'
        })
    ]
    // externals: {
    //     // require("jquery") is external and available
    //     //  on the global var jQuery
    //     "jquery": jQuery
    // }
};

module.exports = config;
