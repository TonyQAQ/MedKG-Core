import React from 'react';
import { Provider } from 'react-redux'
import { ConfigProvider } from 'antd';
import { ConnectedRouter } from 'connected-react-router'
import zh_CN from 'antd/es/locale-provider/zh_CN';
import { HashRouter as Router, Route, Switch, Link } from "react-router-dom";
import moment from 'moment';
import configureStore, { history } from './store'
import AsyncComponent from '@/components/asyncComponent'
import AuthorizedRoute from "./pages/AuthorizedRoute/authorizedRoute";
import 'moment/locale/zh-cn';

const store = configureStore()

const LayoutWrap = AsyncComponent(() => (import('@/pages/LayoutWrap')))
const Login = AsyncComponent(() => (import('@/pages/Login')))
const TaskMngExecutePage = AsyncComponent(() => (import('@/pages/TaskManagement/ExecutePage')))
const MappingRulesPage = AsyncComponent(() => (import('@/pages/TaskExecution/MappingRulesPage')))
const UsMappingRulesPage = AsyncComponent(() => (import('@/pages/TaskExecution/UsMappingRulesPage')))
const MakeRulesPage = AsyncComponent(() => (import('@/pages/TaskManagement/MakeRulesPage')))
const UsMakeRulesPage = AsyncComponent(() => (import('@/pages/TaskManagement/UsMakeRulesPage')))
const ChartsAttach = AsyncComponent(() => (import('@/pages/TaskExecution/ChartsAttach')))
const StructuredKnowlMark = AsyncComponent(() => (import('@/pages/TaskManagement/StructuredKnowlMark')))
const LabelDefinition = AsyncComponent(() => (import('@/pages/TaskExecution/LabelDefinition')))
const FormalMark = AsyncComponent(() => (import('@/pages/TaskExecution/FormalMark')))
const SchemaCheckOut = AsyncComponent(() => (import('@/pages/TaskManagement/SchemaCheckOut')))
const ConfirmGuideLinePage = AsyncComponent(() => (import('@/pages/TaskManagement/ConfirmGuideLinePage')))
const UsMarkingTraining = AsyncComponent(() => (import('@/pages/TaskExecution/UsMarkingTraining')))
const UsMarkTrainingCheck = AsyncComponent(() => (import('@/pages/TaskManagement/UsMarkTrainingCheck')))
const UsResultAcceptance = AsyncComponent(() => (import('@/pages/TaskManagement/UsResultAcceptance')))
const MarkingTaskPage = AsyncComponent(() => (import('@/pages/TaskManagement/MarkingTaskPage')))
const NLPSSTPage = AsyncComponent(() => (import('@/pages/NaturalLanguage/NLPSSTPage')))
const WSTPage = AsyncComponent(() => (import('@/pages/NaturalLanguage/WSTPage')))
const EETPage = AsyncComponent(() => (import('@/pages/NaturalLanguage/EETPage')))
const RETPage = AsyncComponent(() => (import('@/pages/NaturalLanguage/RETPage')))
const CCTPage = AsyncComponent(() => (import('@/pages/NaturalLanguage/CCTPage')))
const MCTPage = AsyncComponent(() => (import('@/pages/NaturalLanguage/MCTPage')))
const Page404 = AsyncComponent(() => (import('@/pages/Page404')))

moment.locale('zh-cn');

function App() {
  return (
    <ConfigProvider locale={zh_CN}>
      <Provider store={store}>
        <ConnectedRouter history={history}>
          <Router>
            <Switch>
              <Route path="/Login" component={Login} history={history} />
              <AuthorizedRoute pageType='03' path="/HKKS" component={LayoutWrap} history={history} />
              {/* 任务管理 */}
              <AuthorizedRoute pageType='01' littleStateCodeArry={true} littleStateCode={['100101', '100201']} stateCode='100000' path="/TaskMngExecutePage/taskId=:taskId" component={TaskMngExecutePage} />
              <AuthorizedRoute pageType='01' littleStateCode='200102' stateCode='200000' path="/MakeRulesPage/taskId=:taskId" component={MakeRulesPage} />
              <AuthorizedRoute pageType='01' littleStateCode='200203' stateCode='200000' path="/UsMakeRulesPage/taskId=:taskId" component={UsMakeRulesPage} />
              <AuthorizedRoute pageType='01' littleStateCode='200204' stateCode='200000' path="/SchemaCheckOut/taskId=:taskId" component={SchemaCheckOut} />
              <AuthorizedRoute pageType='01' littleStateCode='300102' stateCode='300000' path="/StructuredKnowlMark/taskId=:taskId" component={StructuredKnowlMark} />
              <AuthorizedRoute pageType='01' littleStateCodeArry={true} littleStateCode={['300204', '400201']} stateCode='300000' path="/UsResultAcceptance/taskId=:taskId" component={UsResultAcceptance} />
              <AuthorizedRoute pageType='01' littleStateCode='300202' stateCode='300000' path="/UsMarkTrainingCheck/taskId=:taskId" component={UsMarkTrainingCheck} />
              <AuthorizedRoute pageType='03' path="/MarkingTaskPage/taskId=:taskId" component={MarkingTaskPage} />

              {/* 任务执行 */}
              <AuthorizedRoute pageType='02' littleStateCode='200101' stateCode='200000' path="/MappingRulesPage/taskId=:taskId" component={MappingRulesPage} />
              <AuthorizedRoute pageType='02' littleStateCode='200201' stateCode='200000' path="/UsMappingRulesPage/taskId=:taskId" component={UsMappingRulesPage} />
              <AuthorizedRoute pageType='02' littleStateCode='300101' stateCode='300000' path="/ChartsAttach/taskId=:taskId" component={ChartsAttach} />
              <AuthorizedRoute pageType='02' littleStateCode='200202' stateCode='200000' path="/LabelDefinition/taskId=:taskId" component={LabelDefinition} />
              <AuthorizedRoute pageType='02' littleStateCode='300201' stateCode='300000' path="/UsMarkingTraining/taskId=:taskId" component={UsMarkingTraining} />
              <AuthorizedRoute pageType='02' littleStateCode='300203' stateCode='300000' path="/FormalMark/taskId=:taskId" component={FormalMark} />
              {/* 其他 */}
              <AuthorizedRoute pageType='03' path="/ConfirmGuideLinePage/taskId=:taskId" component={ConfirmGuideLinePage} />
              {/* 自然语言 */}
              <AuthorizedRoute pageType='NLP' path="/NLPSSTPage/taskId=:taskId" component={NLPSSTPage} />
              <AuthorizedRoute pageType='NLP' path="/WSTPage/taskId=:taskId" component={WSTPage} />
              <AuthorizedRoute pageType='NLP' path="/EETPage/taskId=:taskId" component={EETPage} />
              <AuthorizedRoute pageType='NLP' path="/RETPage/taskId=:taskId" component={RETPage} />
              <AuthorizedRoute pageType='NLP' path="/CCTPage/taskId=:taskId,fromTaskId=:fromTaskId" component={CCTPage} />
              <AuthorizedRoute pageType='NLP' path="/MCTPage/taskId=:taskId,fromTaskId=:fromTaskId" component={MCTPage} />
              <Route component={Page404} />
            </Switch>
          </Router>
        </ConnectedRouter>
      </Provider>
    </ConfigProvider >
  );
}

export default App;
