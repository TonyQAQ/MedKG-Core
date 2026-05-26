import request from '@/util/request';

//获取数据库配置列表
export async function getLoginInfo(params) {
    return request('/session/isEmpty', {
        params: {
            ...params
        }
    })
}


//查看当前任务的大状态，小状态
export async function getTaskStateInfo(params) {
    return request('/task/task/state', {
        params: {
            ...params
        }
    })
}





