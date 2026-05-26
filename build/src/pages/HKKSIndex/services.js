import request from '@/util/request';

//查询顶部信息
export async function getHeadInfo(params) {
    return request('/task/us/ov/stat/top', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//查询顶部信息
export async function getMiddleList(params) {
    return request('/task/us/ov/stat/mid', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//查询底部信息
export async function getFooterList(params) {
    return request('/task/us/ov/stat/bottom', {
        body: {
            ...params
        },
        method: "POST"
    })
}








