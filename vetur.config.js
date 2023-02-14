module.exports = {
  settings: {
    "vetur.useWorkspaceDependencies": true,
    "vetur.experimental.templateInterpolationService": true,
    "vetur.format.defaultFormatter.html": "prettier",
    "vetur.format.defaultFormatter.css": "prettier",
    "vetur.format.defaultFormatter.postcss": "prettier",
    "vetur.format.defaultFormatter.scss": "prettier",
    "vetur.format.defaultFormatter.js": "prettier",
    "vetur.format.defaultFormatter.sass": "sass-formatter"
  },
  projects: [
    './ui',
  ]
}
