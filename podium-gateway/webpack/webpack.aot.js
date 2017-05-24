/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

const commonConfig = require('./webpack.common.js');
const webpack = require('webpack');
const webpackMerge = require('webpack-merge');
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const Visualizer = require('webpack-visualizer-plugin');
const CompressionPlugin = require("compression-webpack-plugin");
const path = require('path');

module.exports = function(env) {
    console.log("ENV ENVV ENV ENV ", env);
    const envOptions = {
        env: 'prod',
        webpack_env: env.webpack_env || 'jit'
    };

    return webpackMerge(commonConfig(envOptions), {
        entry: {
            'polyfills': './target/unbundled-aot/app/polyfills',
            'global': './src/main/webapp/content/scss/global.scss',
            'main': './target/unbundled-aot/app/app.main.aot'
        },
        profile: true,
        devtool: false,
        output: {
            path: __dirname + "/../target/dist/aot",
            filename: "[name].js",
            publicPath: "./"
        },
        plugins: [
            new webpack.LoaderOptionsPlugin({
                minimize: true,
                debug: false
            }),
            new webpack.optimize.UglifyJsPlugin({
                compress: {
                    warnings: false
                },
                output: {
                    comments: false
                },
                sourceMap: false
            }),
            new CompressionPlugin({
                asset: "[path].gz[query]",
                algorithm: "gzip",
                test: /\.js$|\.html$/,
                threshold: 10240,
                minRatio: 0.8
            }),
            new ExtractTextPlugin('[hash].styles.css'),
            new Visualizer({
                // Webpack statistics in target folder
                filename: '../stats.html'
            })
        ],
        node: {
            __filename: true
        }
    });
}
