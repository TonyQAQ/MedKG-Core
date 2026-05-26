import request from '@/util/request';

//获取任务类型、状态码表
export async function initList(params) {
    return request('/task/search/info', {
        params: {
            ...params
        }
    })
}

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

//确认取消
export async function comfirmCancel(params) {
    return request(`/task/cancelTask`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//任务准备-分句
export async function sstTaskReady(params) {
    return request(`/nlp/sst/ready/sentences`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//任务准备-分词
export async function wstTaskReady(params) {
    return request(`/nlp/wst/ready/words`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//任务准备-实体抽取
export async function eetTaskReady(params) {
    return request(`/nlp/eet/ready/mark`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//任务准备-关系抽取
export async function retTaskReady(params) {
    return request(`/nlp/ret/ready/mark`, {
        body: {
            ...params
        },
        method: "POST"
    })
}
//任务准备-多类别抽取
export async function cctTaskReady(params) {
    return request(`/nlp/cct/ready/mark`, {
        body: {
            ...params
        },
        method: "POST"
    })
}
//任务准备-多标签抽取
export async function mctTaskReady(params) {
    return request(`/nlp/mct/ready/mark`, {
        body: {
            ...params
        },
        method: "POST"
    })
}


//更新任务状态
export async function sstUpdateTaskState(params) {
    return request(`/nlp/common/update/state`, {
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
export async function addMarkTask(params, { taskId, typeCode, taskName, startTime, endTime, memo, fromTaskId }) {
    return request(`/nlp/common/save/task?taskId=${taskId}&taskName=${taskName}&startTime=${startTime}&endTime=${endTime}&memo=${memo}&typeCode=${typeCode}&fromTaskId=${fromTaskId}`, {
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
    return request(`/nlp/common/search/taskEditInfo`, {
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

//获取引用类型表格
export async function taskTables(params) {
    return request(`/nlp/common/search/taskTables`, {
        body: { ...params },
        method: "POST"
    })
}
