import request from '@/util/request';


//获取树
export async function getTreeData(params) {
    return request('/nlp/common/search/tree', {
        body: params,
        method: "POST"
    })
}



//获取SVG列表
export async function getSentence(params) {
    return request(`/nlp/sst/search/sentences`, {
        body: { ...params },
        method: "POST"
    })
}


//更新任务状态
export async function updateTaskStatu(params) {
    return request(`/task/us/ql/update/state`, {
        body: { ...params },
        method: "POST"
    })
}

// 生成任务实例
export async function createInstance(params) {
    return request(`knowledge/createInstance`, {
        params: { ...params }
    })
}

//保存
export async function saveSentence(params) {
    return request(`/nlp/sst/save/sentences`, {
        body: { ...params },
        method: "POST"
    })
}




















