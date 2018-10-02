const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: "./src/index.tsx",
    mode: "development",
    devtool: 'source-map',
    devServer: {
        watchOptions : {
            poll: true
        },
        watchContentBase: true,
        contentBase: path.join(__dirname, 'src'),
        compress: true,
        port: 9000,
        proxy: {
            '/eventbus' : {target: 'http://localhost:8080/', ws : true}
        }
    },
    output: {
        filename: "[name].bundle.js",
        path: path.resolve(__dirname, '../resources/webroot/'),
    },
    plugins: [new HtmlWebpackPlugin({
        title: "Chat",
        template: "src/index.html"
    })],
    resolve: {
        extensions: ['.js', '.json', '.ts', '.tsx'],
    },
    module: {
        rules: [
            {
                test: /\.(ts|tsx)$/,
                loader: "awesome-typescript-loader"
            },
            {
                test: /\.css$/,
                use: [ 'style-loader', 'css-loader' ]
            }
        ]
    }
};