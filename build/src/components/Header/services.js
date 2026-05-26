import request from '@/util/request';

//获取数据库配置列表
export async function loginOut(params) {
    return request('/data/LoginOut.svt', {
        body: {
            ...params
        },
        method:"POST"
    })
}

//获取数据库配置列表
export async function changePwd(params) {
    return request('/task/us/um/update/password', {
        body: {
            ...params
        },
        method:"POST"
    })
}





