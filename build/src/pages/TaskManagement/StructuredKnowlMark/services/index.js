import request from '@/util/request';

//任务准备查询初始化表信息
export async function getReadyTableInfo(params) {
    return request(`/task/rv/check/search/tables`, {
        body: {
            ...params
        },
        method: "POST"
    })
}



//获取表字段预选统计数据
export async function getStatisticsInfo(params) {
    return request('/task/sc/check/search/statData', {
        params: {
            ...params
        }
    })
}

//任务准备查询表字段信息
export async function getTableInfo(params) {
    return request(`/task/rv/check/search/rdf`, {
        body: {
            ...params
        },
        method: "POST"
    })
}


//保存RDF
export async function saveRDF(params, id) {
    return request(`/task/rv/check/saveOrUpdateRDF?tableMappingId=` + id, {
        body: [...params],
        isForm: true,
        method: "POST"
    })
}

//提交确认
export async function confirmSubmitInfo(params) {
    return request(`/task/rv/check/publishConfirm`, {
        body: { ...params },
        method: "POST"
    })
}

//别名、规则映射任务提交
export async function confirmPublish(params) {
    return request(`/task/rv/check/publish/rdf`, {
        body: { ...params },
        method: "POST"
    })
}


export async function createInstance(params) {
    return request(`knowledge/createInstance`, {
        params: { ...params }
    })
}
