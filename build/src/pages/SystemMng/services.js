import request from '@/util/request';

//查询团队码表
export async function getList(params) {
    return request('/task/us/org/search', {
        params: {
            ...params
        }
    })
}

//保存或修改组织信息
export async function saveOrUpdate(params) {
    return request('/task/us/org/saveOrUpdate', {
        body: {
            ...params
        },
        isForm: true,
        method: "POST"
    })
}

//删除组织信息
export async function delTeamInfo(params) {
    return request('/task/us/org/delete', {
        body: {
            ...params
        },
        isForm: true,
        method: "POST"
    })
}

//角色信息查询
export async function getRolesInfo(params) {
    return request('/task/us/um/roles', {
        params: {
            ...params
        },
    })
}

//新增人员
export async function newAddRole(params) {
    return request('/task/us/um/save', {
        body: {
            ...params
        },
        isForm: true,
        method: "POST"
    })
}


//查询人员列表
export async function getRoleTable(params) {
    return request('/task/us/um/search', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//编辑人员信息
export async function editRoleInfo(params) {
    return request('/task/us/um/update', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//删除人员信息
export async function delRoleInfo(params) {
    return request('/task/us/um/delete', {
        body: {
            ...params
        },
        method: "POST"
    })
}







