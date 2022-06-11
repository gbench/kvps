const kvps_SERVER_HOST = "http://localhost:8090";

module.exports = {
	mode: "development",
	// kvps-开发服务器的设置
	devServer: {
		hot: true,
		proxy: {
			'/kvps/api': {// 转换 kvps：kvps的服务端口是8090
				target: kvps_SERVER_HOST,
				secure: true,
				logLevel: 'debug',
				changeOrigin: true, //   必须加入否则会导致webpack奔溃
				pathRewrite: { '^/kvps/api': '/kvps/api' },//去除接口标记
			}, // kvps 服务器
			'/kvps/plan': {// 转换 kvps：kvps的服务端口是8090
				target: kvps_SERVER_HOST,
				secure: true,
				logLevel: 'debug',
				changeOrigin: true, //   必须加入否则会导致webpack奔溃
				pathRewrite: { '^/kvps/plan': '/kvps/plan' },//去除接口标记
			}, // kvps 服务器
			'/kvps/activiti': {// 转换 kvps：kvps的服务端口是8090
				target: kvps_SERVER_HOST,
				secure: true,
				logLevel: 'debug',
				changeOrigin: true, //   必须加入否则会导致webpack奔溃
				pathRewrite: { '^/kvps/activiti': '/kvps/activiti' },//去除接口标记
			}, // kvps 服务器
			'/kvps/media': {// 转换 kvps：kvps的服务端口是8090
				target: kvps_SERVER_HOST,
				secure: true,
				logLevel: 'debug',
				changeOrigin: true, //   必须加入否则会导致webpack奔溃
				pathRewrite: { '^/kvps/media': '/kvps/media' },//去除接口标记
			}, // kvps 服务器
		},// proxy
	}// devServer
}
