import request from '@/util/request';

//获取任务类型、状态码表
export async function getTaskTypeList(params) {
    return request('/task/codeTable/stateAndType', {
        params: {
            ...params
        }
    })
}

//任务多条件查询接口
export async function getTableList(params) {
    return request('/task/search/info', {
        params: {
            ...params
        }
    })
}

//获取数据库配置列表
export async function getDataSourceList(params) {
    return request('/db/search/database', {
        params: {
            ...params
        }
    })
}

//删除任务
export async function deleteTask(params) {
    return request('/task/delete/info', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//人员查询
export async function queryUsers(params) {
    return request('/task/query/users', {
        params: {
            ...params
        }
    })
}


//新增或编辑
export async function addOrEdit(params, userIds) {
    return request(`/task/addOrUpdate/info?userIds=${userIds}`, {
        body: {
            ...params
        },
        isForm: true,
        method: "POST"
    })
}

//数据源码表
export async function dataSourceTimer(params) {
    return request(`/db/codeTable/database`, {
        params: {
            ...params
        }
    })
}

//判断能否删除
export async function judgeDelete(params) {
    return request(`/task/delete/state`, {
        params: {
            ...params
        }
    })
}

//确认删除
export async function comfirmDelete(params) {
    return request(`/task/delete/info`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//判断能否取消
export async function judgeCancel(params) {
    return request(`/task/cancel/state`, {
        params: {
            ...params
        }
    })
}

//确认取消
export async function comfirmCancel(params) {
    return request(`/task/cancelTask`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//任务准备
export async function taskReady(params) {
    return request(`/task/ready/exeReady`, {
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
    return request(`/task/ssm/exe/saveOrUpdate/Columns`, {
        body: [...params],
        isForm: true,
        method: "POST"
    })
}

//别名、规则映射任务提交
export async function submitRulesInfo(params) {
    return request(`/task/ssm/exe/submit`, {
        body: { ...params },
        method: "POST"
    })
}


//新增标注任务
export async function addMarkTask(params, { taskId, typeCode, taskName, startTime, endTime, memo }) {
    return request(`/task/us/ql/save/task?taskId=${taskId}&taskName=${taskName}&startTime=${startTime}&endTime=${endTime}&memo=${memo}&typeCode=${typeCode}`, {
        body: { ...params },
        isUpload: true,
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

//任务准备
export async function readyTask(params) {
    return request(`/task/us/ql/ready/mark`, {
        body: { ...params },
        method: "POST"
    })
}

//typeCode = 04 获取编辑信息
export async function getEditInfo(params) {
    return request(`/task/us/ql/search/taskEditInfo`, {
        body: { ...params },
        method: "POST"
    })
}

//初始化模型列表
export async function initAiList(params) {
    return request(`/label/model/info`, {
        body: { ...params },
        method: "GET"
    })
}

//训练模型
export async function trainingModel(params) {
    return request(`/label/task/model/save`, {
        params: { ...params }
    })
}













