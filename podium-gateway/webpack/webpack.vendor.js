/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

var webpack = require('webpack');
const path = require('path');

/*
 All vendor components excluding the angular compiler.
 The compiler is not required for AOT compilation.
 */
const vendorpack = [
    './src/main/webapp/app/vendor',
    '@angular/common',
    '@angular/core',
    '@angular/forms',
    '@angular/http',
    '@angular/platform-browser',
    '@angular/router',
    '@ng-bootstrap/ng-bootstrap',
    'angular2-cookie',
    'ngx-infinite-scroll',
    'jquery',
    'ng-jhipster',
    'ng2-webstorage',
    'rxjs'
];

var targetPath = "./target/www";

module.exports = function(env) {
    if (env.webpack_env === 'jit') {
        vendorpack.push(
            '@angular/compiler',
            '@angular/platform-browser-dynamic'
        );
    } else if (env.webpack_env === 'aot') {
        targetPath = './target/dist';
    }

    return {
		entry: {
			'vendor': vendorpack
		},
		resolve: {
			extensions: ['.ts', '.js'],
			modules: ['node_modules']
		},
		module: {
			exprContextCritical: false,
			rules: [
				{
					test: /(vendor\.scss|global\.scss)/,
					loaders: ['style-loader', 'css-loader', 'postcss-loader', 'sass-loader']
				},
				{
					test: /\.(jpe?g|png|gif|svg|woff|woff2|ttf|eot)$/i,
					loaders: [
						'file-loader?hash=sha512&digest=hex&name=[hash].[ext]',
						'image-webpack-loader?bypassOnDebug&optimizationLevel=7&interlaced=false'
					]
				}
			]
		},
		output: {
			filename: '[name].dll.js',
			path: path.resolve(targetPath),
			library: '[name]'
		},
		plugins: [
			new webpack.DllPlugin({
				name: '[name]',
				path: path.resolve(targetPath + '/[name].json')
			})
		]
	}
}
