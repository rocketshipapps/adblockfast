const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
const HtmlPlugin = require('html-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');

module.exports = {
  entry: {
    popup: path.resolve('src/popup/popup.tsx'),
    service_worker: path.resolve('src/service_worker.ts'),
    content: path.resolve('src/content.ts'),
  },
  module: {
    rules: [
      { use: 'ts-loader', test: /\.tsx?$/, exclude: /node_modules/ },
      { use: ['style-loader', 'css-loader'], test: /\.css$/i },
      {
        type: 'asset/resource',
        test: /\.(png|svg|jpg|jpeg|gif|woff|woff2|eot|ttf)$/,
      },
    ],
  },
  plugins: [
    new CleanWebpackPlugin({
      cleanStaleWebpackAssets: false,
    }),
    new CopyPlugin({
      patterns: [
        {
          from: path.resolve('src/manifest.json'),
          to: path.resolve('dist/manifest.json'),
        },
        {
          from: path.resolve('img'),
          to: path.resolve('dist/img'),
        },
        {
          from: path.resolve('filter'),
          to: path.resolve('dist/filter'),
        },
      ],
    }),
    ...getHtmlPlugins(['popup']),
  ],
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
  },
  output: {
    filename: '[name].js',
    path: path.resolve('dist'),
  },
  optimization: {
    splitChunks: {
      chunks: 'all',
    },
  },
};

function getHtmlPlugins(chunks) {
  return chunks.map(
    (chunk) =>
      new HtmlPlugin({
        title: 'Adblock Fast',
        filename: `${chunk}.html`,
        chunks: [chunk],
      }),
  );
}
