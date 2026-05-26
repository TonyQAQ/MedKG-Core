import { NEW_ADD_OR_CANCEL, SET_EDIT_INFO, SET_PAGE, RESET_STATE, SET_TYPECODE } from './actionTypes'

const initialState = {
    showPage: 1,
    pageCount: 1,
    pageSize: 10,
    editInfo: '',
    isEdit: false
}

export default (state = initialState, { type, payload }) => {
    let newState = JSON.parse(JSON.stringify(state));
    switch (type) {

        case NEW_ADD_OR_CANCEL: {
            const { showPage } = payload
            newState.showPage = showPage
            newState.isEdit = false
            break;
        }
        case SET_EDIT_INFO: {
            const { info } = payload
            newState.isEdit = true
            newState.editInfo = Object.assign({}, info)
            break;
        }
        case SET_PAGE: {
            const { page, pageSize } = payload
            newState.pageCount = page
            newState.pageSize = pageSize
            break;
        }
        case SET_TYPECODE: {
            const { value } = payload
            newState.typeCode = value
            break;
        }
        case RESET_STATE: {
            newState = initialState
            break;
        }

    }
    return newState
}
