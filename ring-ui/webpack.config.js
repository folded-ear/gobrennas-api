const {join, resolve} = require('path');

const ringUiWebpackConfig = require('@jetbrains/ring-ui/webpack.config');

const pkgConfig = require('./package.json').config;

const componentsPath = join(__dirname, pkgConfig.components);

// Patch @jetbrains/ring-ui svg-sprite-loader config
ringUiWebpackConfig.loaders.svgInlineLoader.include.push(
  require('@jetbrains/logos'),
  require('@jetbrains/icons')
);

const webpackConfig = () => ({
  entry: `${componentsPath}/app/app.js`,
  resolve: {
    mainFields: ['module', 'browser', 'main'],
    alias: {
      react: resolve('./node_modules/react'),
      'react-dom': resolve('./node_modules/react-dom'),
      '@jetbrains/ring-ui': resolve('./node_modules/@jetbrains/ring-ui')
    }
  },
  output: {
    path: resolve(__dirname, pkgConfig.dist),
    filename: 'ring-ui.js',
    publicPath: '',
    library: 'RingUI',
    libraryTarget: 'umd',
    devtoolModuleFilenameTemplate: '/[absolute-resource-path]'
  },
  externals: {
    react: {
      commonjs: 'react',
      commonjs2: 'react',
      amd: 'react',
      root: 'React'
    },
    'react-dom': {
      commonjs: 'react-dom',
      commonjs2: 'react-dom',
      amd: 'react-dom',
      root: 'ReactDOM'
    }
  },
  module: {
    rules: [
      ...ringUiWebpackConfig.config.module.rules,
      {
        test: /\.css$/,
        include: componentsPath,
        use: [
          'style-loader',
          {loader: 'css-loader'},
          {loader: 'postcss-loader'}
        ]
      },
      {
        // Loaders for any other external packages styles
        test: /\.css$/,
        include: /node_modules/,
        exclude: ringUiWebpackConfig.componentsPath,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.js$/,
        include: [componentsPath],
        loader: 'babel-loader?cacheDirectory'
      }
    ]
  },
  devServer: {
    stats: {
      assets: false,
      children: false,
      chunks: false,
      hash: false,
      version: false
    }
  }
});

module.exports = webpackConfig;
