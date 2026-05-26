import request from '@/util/request';
//图谱初始化
export async function initAtlas(params) {
    return request('/knowledge/show', {
        params: {
            ...params
        }
    })
}
//图谱列表
export async function getList(params) {
    return request('/map/search/taskMap', {
        params: {
            ...params
        }
    })
}