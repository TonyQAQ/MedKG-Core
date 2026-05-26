import request from '@/util/request';


//获取树
export async function getTreeData(params) {
    return request('/nlp/common/search/tree', {
        body: params,
        method: "POST"
    })
}



//获取分词列表
export async function getWstList(params) {
    return request(`/nlp/wst/search/words`, {
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
        body: params,
        isForm: true,
        method: "POST"
    })
}
//保存分词
export async function saveWords(params, taskId, paths) {
    return request(`/nlp/wst/save/words?taskId=${taskId}&paths=${paths}`, {
        body: params,
        isForm: true,
        method: "POST"
    })
}




















