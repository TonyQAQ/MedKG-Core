import request from '@/util/request';

//获取数据库配置列表
export async function login(params) {
    return request('/data/Login.svt', {
        body: {
            ...params
        },
        method:"POST"
    })
}

//获取数据库配置列表
export async function getLoginInfo(params) {
    return request('/session/isEmpty', {
        params: {
            ...params
        }
    })
}




