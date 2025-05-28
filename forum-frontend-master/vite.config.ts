import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import styleImport, {VantResolve} from 'vite-plugin-style-import';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue(), styleImport({
        resolves: [VantResolve()],
    }),]
})
// 'use strict'
// const path = require('path')
// const resolve = dir => path.join(__dirname, dir)
//
// // 其他配置
//
// module.exports = {
//     // 其他配置
//
//     configureWebpack: {
//         name: name,
//         devtool: 'source-map',
//         resolve: {
//             alias: {
//                 '@': resolve('src')
//             }
//         }
//     }
// }
