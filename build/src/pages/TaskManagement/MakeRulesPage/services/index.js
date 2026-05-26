import request from '@/util/request';

//任务准备查询初始化表信息
export async function getReadyTableInfo(params) {
    return request(`/task/sc/check/search/tables`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//查询指定字段下的数据
export async function getHoverList(params) {
    return request('/db/search/columnData', {
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
    return request(`/task/sc/check/search/columns`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//过滤规则码表
export async function tableRules(params) {
    return request('/task/ssm/exe/search/filterCodeTable', {
        params: {
            ...params
        }
    })
}

//保存别名和规则映射
export async function saveRulesInfo(params) {
    return request(`/task/sc/check/saveOrUpdate/columns`, {
        body: [...params],
        isForm: true,
        method: "POST"
    })
}

//提交确认
export async function confirmSubmitInfo(params) {
    return request(`/task/sc/check/publishConfirm`, {
        body: { ...params },
        method: "POST"
    })
}

//别名、规则映射任务提交
export async function confirmPublish(params) {
    return request(`/task/sc/check/publish`, {
        body: { ...params },
        method: "POST"
    })
}
