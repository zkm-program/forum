import axios, {AxiosInstance} from "axios";
import {useRouter} from "vue-router";

const isDev = process.env.NODE_ENV === 'development';

const myAxios: AxiosInstance = axios.create({
    baseURL: isDev ? 'http://localhost:8101/api' : '线上地址',
});

myAxios.defaults.withCredentials = true;
const router = useRouter();
// Add a request interceptor
myAxios.interceptors.request.use(function (config) {
    // console.log('我要发请求啦', config)
    // Do something before request is sent
    return config;
}, function (error) {
    // Do something with request error
    return Promise.reject(error);
});

// Add a response interceptor
myAxios.interceptors.response.use(function (response) {
    // console.log('我收到你的响应啦', response)
    if (response?.data?.code === 40100) {
        // const redirectUrl = encodeURIComponent(window.location.href);
        // router.push(`/user/login?redirect=${redirectUrl}`);
        // const redirectUrl = window.location.href;
        // window.location.href = `/user/login?redirect=${redirectUrl}`;
        // window.location.href = `/user/login`;
    }
    // Do something with response data
    return response.data;
}, function (error) {
    // Do something with response error
    return Promise.reject(error);
});

export default myAxios;
