import { NEW_ADD_OR_CANCEL, SET_EDIT_INFO, SET_PAGE, RESET_STATE } from './actionTypes'

// 改变当前显示组件（1：表格页；2：编辑页或者创建页）
export const newAddOrCancel = (showPage) => {
    return {
        type: NEW_ADD_OR_CANCEL,
        payload: {
            showPage
        }
    }
}

//保存编辑时的id
export const setEditInfo = (info) => {
    return {
        type: SET_EDIT_INFO,
        payload: {
            info
        }
    }
}

// 翻页
export const setPage = (page, pageSize) => {
    return {
        type: SET_PAGE,
        payload: {
            page, pageSize
        }
    }
}

// 重置State
export const resetState = () => {
    return {
        type: RESET_STATE,
        payload: {}
    }
}