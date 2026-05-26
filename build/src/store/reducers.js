import { combineReducers } from 'redux';
import { connectRouter } from 'connected-react-router';
import DataSourceMngReducer from '@/pages/DataSourceMng/store/reducer'
import TaskMngReducer from '@/pages/TaskManagement/store/reducer'
import TaskExeReducer from '@/pages/TaskExecution/store/reducer'
import LoginReducer from '@/pages/Login/store/reducer'
import NLPTaskTaskReducer from '@/pages/NaturalLanguage/NLPTask/store/reducer'

const createRootReducer = (history) => combineReducers({
    router: connectRouter(history),
    DataSourceMngReducer,
    TaskMngReducer,
    TaskExeReducer,
    LoginReducer,
    NLPTaskTaskReducer
});
export default createRootReducer;
