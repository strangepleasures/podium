/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

const webpack = require('webpack');
const CommonsChunkPlugin = require('webpack/lib/optimize/CommonsChunkPlugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const StringReplacePlugin = require('string-replace-webpack-plugin');
const AddAssetHtmlPlugin = require('add-asset-html-webpack-plugin');
const path = require('path');

var targetPath = "./target/www";

module.exports = function (envOptions) {
    console.log('OPTIONS ', envOptions);
    if (envOptions.webpack_env === 'aot') {
        targetPath = './target/dist';
    }

    const DATAS = {
        VERSION: JSON.stringify(require("../package.json").version),
        DEBUG_INFO_ENABLED: envOptions.env === 'dev'
    };

    var common = {
        resolve: {
            extensions: ['.ts', '.js'],
            modules: ['node_modules']
        },
        module: {
			rules: [
				{ test: /bootstrap\/dist\/js\/umd\//, loader: 'imports-loader?jQuery=jquery' },
				{
					test: /\.ts$/,
					loaders: [
						'angular2-template-loader',
						'awesome-typescript-loader'
					]
				},
				{
					test: /\.html$/,
					loader: 'html-loader',
					options: {
						minimize: true,
						caseSensitive: true,
						removeAttributeQuotes:false,
						minifyJS:false,
						minifyCSS:false
					},
					exclude: ['./src/main/webapp/index.html']
				},
				{
					test: /\.scss$/,
					loaders: ['raw-loader', 'css-loader', 'sass-loader'],
					exclude: /(vendor\.scss|global\.scss)/
				},
				{
					test: /(vendor\.scss|global\.scss)/,
					loaders: ['style-loader', 'css-loader', 'postcss-loader', 'sass-loader']
				},
				{
					test: /\.css$/,
					loaders: ['raw-loader', 'css-loader'],
					exclude: /(vendor\.css|global\.css)/
				},
				{
					test: /(vendor\.css|global\.css)/,
					loaders: ['style-loader', 'css-loader']
				},
				{
					test: /\.(jpe?g|png|gif|svg|woff2?|ttf|eot)$/i,
					loaders: ['file-loader?hash=sha512&digest=hex&name=[hash].[ext]']
				},
				{
					test: /app.constants.ts$/,
					loader: StringReplacePlugin.replace({
						replacements: [{
							pattern: /\/\* @toreplace (\w*?) \*\//ig,
							replacement: function (match, p1, offset, string) {
								return `_${p1} = ${DATAS[p1]};`;
							}
						}]
					})
				}
			]
		},
		plugins: [
			new CommonsChunkPlugin({
				names: ['manifest', 'polyfills'].reverse()
			}),
            new CopyWebpackPlugin([
                { from: './node_modules/core-js/client/shim.min.js', to: 'core-js-shim.min.js' },
                { from: './node_modules/swagger-ui/dist', to: 'swagger-ui/dist' },
                { from: './src/main/webapp/swagger-ui/', to: 'swagger-ui' },
                { from: './src/main/webapp/favicon.ico', to: 'favicon.ico' },
                { from: './src/main/webapp/robots.txt', to: 'robots.txt' },
                { from: './src/main/webapp/i18n', to: 'i18n' }
            ]),
            new webpack.ProvidePlugin({
		    	$: "jquery",
			    jQuery: "jquery"
		    }),
			new StringReplacePlugin()
		]
	};

    if (envOptions.webpack_env === 'jit') {
        common.plugins.push(
            new webpack.DllReferencePlugin({
                context: './',
                manifest: require(path.resolve(targetPath+'/vendor.json'))
            }),
            new AddAssetHtmlPlugin([
                { filepath: path.resolve(targetPath+'/vendor.dll.js'), includeSourcemap: false }
            ]),
            new HtmlWebpackPlugin({
                template: './src/main/webapp/index.html',
                chunksSortMode: 'dependency',
                inject: 'body'
            })
        )
    } else if (envOptions.webpack_env === 'aot') {
        common.plugins.push(
            new HtmlWebpackPlugin({
                template: './src/main/webapp/index-aot.html',
                inject: 'body'
            })
        )
    }

    return common;

};
