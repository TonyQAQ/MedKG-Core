// import React, { Component, Fragment } from 'react';
// import LayoutHeader from '@/components/Header';
// import { Select, Spin, Tooltip, Empty, Input, Modal, Button, Typography, Radio } from 'antd';
// import { MyButton } from '@/components/commonWrap';
// import { Annotator, Action } from 'poplar-annotation';
// import * as api from './services';
// import $ from 'jquery';
// import ColorPicker from 'rc-color-picker';
// import debounce from 'lodash/debounce';
// import 'rc-color-picker/assets/index.css';
// import '../index.less';

// const { Option } = Select;
// const { Title } = Typography;

// export default class index extends Component {
//     constructor(props) {
//         super(props);
//         this.state = {
//             currentSelectIndex: 0,
//             liList: ['11111111111111111111111111111111', '22222222222222222222222222'],
//             spinning: true,
//             taskId: '',
//             visible: false,
//             visible2: false,
//             labelValue: '',
//             svgAllInfo: {},
//             currentSvg: null,
//             publishState: '02',
//             labelCategories: [{
//                 id: 0,
//                 text: "名词",
//                 color: "#eac0a2",
//             },
//             {
//                 id: 1,
//                 text: "名词2",
//                 color: "#eac0a2"
//             }],
//             radioList: [{
//                 id: 0,
//                 text: "名词",
//                 color: "#eac0a2"
//             },
//             {
//                 id: 1,
//                 text: "名词2",
//                 color: "#eac0a2"
//             }],
//             connectionCategories: [{
//                 id: 0,
//                 text: "修饰"
//             },
//             {
//                 id: 1,
//                 text: "限定"
//             },
//             {
//                 id: 2,
//                 text: "是...的动作"
//             }],
//             radioConnectionList: [{
//                 id: 0,
//                 text: "修饰"
//             },
//             {
//                 id: 1,
//                 text: "限定"
//             },
//             {
//                 id: 2,
//                 text: "是...的动作"
//             }],
//             radioValue: null,
//             radioInputVal: null,
//             color: '#1890FF',
//             startIndex: null,
//             endIndex: null,
//             first: null,
//             second: null,
//             fromLabel: null,
//             endLabel: null
//         }
//         this.searchInput = debounce(this.searchInput.bind(this), 500)
//     }


//     componentDidMount = () => {
//         const { taskId } = this.props.match.params
//         this.setState({
//             taskId
//         })
//     }

//     liClick = () => {

//     }

//     searchInput = (type, value) => {
//         if (type === 'search1') {
//             let tempLabelArry
//             if (value) {
//                 tempLabelArry = this.state.labelCategories.filter(item => item.text.indexOf(value) > -1)
//             } else {
//                 tempLabelArry = JSON.parse(JSON.stringify(this.state.labelCategories))
//             }
//             this.setState({
//                 radioList: tempLabelArry,
//                 radioInputVal: value,
//                 radioValue: null
//             })
//         } else if (type === 'search2') {
//             let tempLabelArry
//             if (value) {
//                 tempLabelArry = this.state.connectionCategories.filter(item => item.text.indexOf(value) > -1)
//             } else {
//                 tempLabelArry = JSON.parse(JSON.stringify(this.state.connectionCategories))
//             }
//             this.setState({
//                 radioConnectionList: tempLabelArry,
//                 radioInputVal: value,
//                 radioValue: null
//             })
//         }

//     }

//     debounceSearchInput(type, { target: { value } }) {
//         this.searchInput(type, value)
//     }

//     selChange(type, value) {
//         if (type === 'label') {
//             this.setState({
//                 labelValue: value
//             })
//         }
//     }

//     btnClick(type) {
//         const { taskId } = this.state
//         if (type === 'pre') {
//             api.preClick({
//                 taskId
//             }).then(res => {

//             }).catch(err => { })
//         }
//     }

//     initSvg = (svgAllInfo, currentSvg, startIndex, endIndex) => {
//         for (const key in svgAllInfo) {
//             let tempJson = svgAllInfo[key].store.json
//             tempJson.labelCategories = this.state.labelCategories
//             tempJson.connectionCategories = this.state.connectionCategories
//             svgAllInfo[key].remove()
//             svgAllInfo[key] = null
//             svgAllInfo[key] = new Annotator(tempJson, document.getElementById('svgContainer' + key), {
//                 labelWidthCalcMethod: 'label'
//             })
//             svgAllInfo[key].on('textSelected', (startIndex, endIndex) => {
//                 console.log(key)
//                 this.setState({
//                     visible: true,
//                     currentSvg: key,
//                     startIndex,
//                     endIndex
//                 })
//             });
//             svgAllInfo[key].on('labelClicked', (id, event) => {
//                 // 输出用户点击的label的ID
//                 console.log(id);
//             });
//             svgAllInfo[key].on('twoLabelsClicked', (first, second) => {
//                 this.setState({
//                     visible2: true,
//                     currentSvg: key
//                 }, () => {
//                     const { currentSvg } = this.state
//                     let fromLabel = ''
//                     let endLabel = ''
//                     if (svgAllInfo[currentSvg]) {
//                         svgAllInfo[currentSvg].store.json.labels.map(item => {
//                             if (item.id === first) {
//                                 fromLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
//                             } else if (item.id === second) {
//                                 endLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
//                             }
//                         })
//                     }
//                     this.setState({
//                         first,
//                         second,
//                         fromLabel,
//                         endLabel
//                     })
//                 })
//             });
//             svgAllInfo[key].on('connectionRightClicked', (id, event) => {
//                 // 输出用户点击的Connection的ID, 点击鼠标的event
//                 console.log(id, event);
//                 const { currentSvg } = this.state
//                 svgAllInfo[currentSvg].applyAction(Action.Connection.Delete(id))
//                 for (const key in svgAllInfo) {
//                     if (svgAllInfo.hasOwnProperty(key)) {
//                         console.log(svgAllInfo[key].store)

//                     }
//                 }
//             });
//             svgAllInfo[key].on('labelRightClicked', (id, x, y) => {
//                 // 输出用户点击的label的ID, 被点击时鼠标的 X,Y 值
//                 console.log(id, x, y);
//                 const { currentSvg } = this.state
//                 svgAllInfo[currentSvg].applyAction(Action.Label.Delete(id))
//                 for (const key in svgAllInfo) {
//                     if (svgAllInfo.hasOwnProperty(key)) {
//                         console.log(svgAllInfo[key].store)

//                     }
//                 }
//             });
//             if (key === currentSvg.toString()) {
//                 svgAllInfo[key].applyAction(Action.Label.Create(this.state.labelCategories.length - 1, startIndex, endIndex))
//             }
//         }
//     }

// renderSvg = (item, index) => {
//     let svgDom = document.getElementById('svgContainer' + index)
//     let { svgAllInfo } = this.state
//     const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
//     const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
//     if (svgDom && $('#svgContainer' + index).children().length === 0) {
//         let annotator = new Annotator({
//             content: "北冥有鱼，其名为鲲。鲲之大，不知其几千里也；化而为鸟，其名为鹏。鹏之背，不知其几千里也；怒而飞，其翼若垂天之云。是鸟也，海运则将徙于南冥。南冥者，天池也。《齐谐》者，志怪者也。《谐》之言曰：“鹏之徙于南冥也，水击三千里，抟扶摇而上者九万里，去以六月息者也。”野马也，尘埃也，生物之以息相吹也。",
//             labelCategories: tempArry,
//             labels: [],
//             connectionCategories: tempArry2,
//             connections: []
//         }, document.getElementById('svgContainer' + index), {
//             labelWidthCalcMethod: 'max'
//         })

//         annotator.on('textSelected', (startIndex, endIndex) => {
//             // 输出用户选取的那些字
//             // let userChoosedCategoryId = getUserChoosedCategoryId();
//             // annotator.applyAction(Action.Label.Create(1, startIndex, endIndex))
//             this.setState({
//                 visible: true,
//                 currentSvg: index,
//                 startIndex,
//                 endIndex
//             })
//         });
//         annotator.on('labelClicked', (id, event) => {
//             // 输出用户点击的label的ID
//             console.log(id);
//         });
//         annotator.on('twoLabelsClicked', (first, second) => {

//             this.setState({
//                 visible2: true,
//                 currentSvg: index
//             }, () => {
//                 const { currentSvg } = this.state
//                 let fromLabel = ''
//                 let endLabel = ''
//                 if (svgAllInfo[currentSvg]) {
//                     svgAllInfo[currentSvg].store.json.labels.map(item => {
//                         if (item.id === first) {
//                             fromLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
//                         } else if (item.id === second) {
//                             endLabel = svgAllInfo[currentSvg].store.json.content.substring(item.startIndex, item.endIndex)
//                         }
//                     })
//                 }
//                 this.setState({
//                     first,
//                     second,
//                     fromLabel,
//                     endLabel
//                 })
//             })
//         })
//         annotator.on('connectionRightClicked', (id, event) => {
//             // 输出用户点击的Connection的ID, 点击鼠标的event
//             console.log(id, event);
//             const { currentSvg } = this.state
//             svgAllInfo[currentSvg].applyAction(Action.Connection.Delete(id))
//             for (const key in svgAllInfo) {
//                 if (svgAllInfo.hasOwnProperty(key)) {
//                     console.log(svgAllInfo[key].store)

//                 }
//             }
//         });
//         annotator.on('labelRightClicked', (id, x, y) => {
//             // 输出用户点击的label的ID, 被点击时鼠标的 X,Y 值
//             console.log(id, x, y);
//             const { currentSvg } = this.state
//             svgAllInfo[currentSvg].applyAction(Action.Label.Delete(id))
//             for (const key in svgAllInfo) {
//                 if (svgAllInfo.hasOwnProperty(key)) {
//                     console.log(svgAllInfo[key].store)

//                 }
//             }
//         });
//         svgAllInfo[index] = annotator
//         this.setState({
//             svgAllInfo
//         })
//     }

// }

//     modalOk(type) {
//         const { radioValue, radioInputVal, color, svgAllInfo, currentSvg, startIndex, endIndex, first, second } = this.state
//         if (type === '1') {
//             if (radioValue === '' || radioValue === undefined || radioValue === null) {
//                 this.state.labelCategories.push({
//                     id: this.state.labelCategories.length,
//                     text: radioInputVal,
//                     color
//                 })
//                 this.setState({
//                     labelCategories: this.state.labelCategories,
//                     radioList: JSON.parse(JSON.stringify(this.state.labelCategories)),
//                     visible: false,
//                     radioValue: null,
//                     radioInputVal: null,
//                     color: '#1890FF',
//                     radioList: this.state.labelCategories
//                 }, () => {
//                     this.initSvg(svgAllInfo, currentSvg, startIndex, endIndex)
//                     // console.log(this.state.labelCategories)
//                     // for (const key in svgAllInfo) {
//                     //     if (svgAllInfo.hasOwnProperty(key)) {
//                     //         console.log(svgAllInfo[key].store)

//                     //     }
//                     // }
//                 })
//             } else {
//                 svgAllInfo[currentSvg].applyAction(Action.Label.Create(radioValue, startIndex, endIndex))
//                 this.setState({
//                     visible: false,
//                     radioValue: null,
//                     radioInputVal: null,
//                     color: '#1890FF',
//                     radioList: this.state.labelCategories
//                 })
//             }
//         } else {
//             svgAllInfo[currentSvg].applyAction(Action.Connection.Create(radioValue, first, second))
//             this.setState({
//                 visible2: false,
//                 radioValue: null,
//                 radioInputVal: null,
//                 color: '#1890FF',
//                 radioConnectionList: this.state.connectionCategories
//             })
//         }
//     }


//     modalCancel = () => {
//         this.setState({
//             visible: false,
//             visible2: false,
//             radioValue: null,
//             startIndex: null,
//             endIndex: null,
//             fromLabel: null,
//             endLabel: null,
//             radioList: this.state.labelCategories
//         })
//     }

//     colorChange = (value) => {
//         this.setState({
//             color: value.color
//         })
//     }

//     radioChange({ target: { value } }) {
//         this.setState({
//             radioValue: value
//         })
//     }

//     render() {
//         const {
//             currentSelectIndex, liList, spinning, visible, radioValue,
//             radioList, visible2, fromLabel, endLabel, radioConnectionList
//         } = this.state
//         return (
// <div className='mds-all-layout-wrap' >
//     <LayoutHeader />
// <div className='mds-executePage-wrap'>
//     <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
//                         <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
//                             <div className='mds-executePage-leftWrap' id='tooltipWrap'>
//                                 <div className='mds-executePage-conditionWrapL'>
//                                     <Select onSelect={() => this.selChange('leftSel')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
//                                         <Option value='all'>全部</Option>
//                                         <Option value='yes'>已选择</Option>
//                                         <Option value='no'>未选择</Option>
//                                     </Select>
//                                     <Input placeholder='请输入表名' onChange={this.searchInput.bind(this, 'tableName')} />
//                                 </div>
//                                 <ul className='mds-executePage-ulWrap'>
//                                     {
//                                         liList.length > 0
//                                             ?
//                                             liList.map((item, index) => {
//                                                 return (
//                                                     <Tooltip
//                                                         key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
//                                                         getPopupContainer={() => document.getElementById('tooltipWrap')}
//                                                         overlayClassName='liToolTip'
//                                                         placement='right'
//                                                         title={
//                                                             <div dangerouslySetInnerHTML={{ __html: item.levitationTips }}>
//                                                             </div>
//                                                         }
//                                                     >
//                                                         <li
//                                                             onClick={() => this.liClick(item, index)}
//                                                             className={
//                                                                 currentSelectIndex === index
//                                                                     ?
//                                                                     'mds-executePage-liWrap mds-executePage-liCurrentSelect'
//                                                                     :
//                                                                     (
//                                                                         item.isSelect === 'no'
//                                                                             ?
//                                                                             'mds-executePage-liWrap mds-executePage-liNoSelect'
//                                                                             :
//                                                                             'mds-executePage-liWrap mds-executePage-liSelect'
//                                                                     )

//                                                             }
//                                                         >
//                                                             <span className='mds-executePage-liName'>{item.tableName}</span>
//                                                         </li>
//                                                     </Tooltip>
//                                                 )
//                                             })
//                                             :
//                                             <Empty style={{ marginTop: '35px' }} />
//                                     }
//                                 </ul>
//                             </div>
//                         </Spin>
// <div className='mds-executePage-middleWrap'>
//     {
//         liList.map((item, index) => {
//             return (
//                 <div className='svgContainer' id={`svgContainer${index}`}>
//                     {this.renderSvg(item, index)}
//                 </div>
//             )
//         })
//     }
//     <div style={{ padding: '10px' }}>

//     </div>
// </div>
//                         <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
//                             <div className='mds-executePage-rightWrap' id='tooltipWrap'>
//                                 <div className='mds-executePage-conditionWrapL'>
//                                     <Select onSelect={() => this.selChange('leftSel')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
//                                         <Option value='all'>全部</Option>
//                                         <Option value='yes'>已选择</Option>
//                                         <Option value='no'>未选择</Option>
//                                     </Select>
//                                     <Input placeholder='请输入表名' onChange={this.searchInput.bind(this, 'tableName')} />
//                                 </div>
//                                 <ul className='mds-executePage-ulWrap'>
//                                     {
//                                         liList.length > 0
//                                             ?
//                                             liList.map((item, index) => {
//                                                 return (
//                                                     <Tooltip
//                                                         key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
//                                                         getPopupContainer={() => document.getElementById('tooltipWrap')}
//                                                         overlayClassName='liToolTip'
//                                                         placement='right'
//                                                         title={
//                                                             <div dangerouslySetInnerHTML={{ __html: item.levitationTips }}>
//                                                             </div>
//                                                         }
//                                                     >
//                                                         <li
//                                                             onClick={() => this.liClick(item, index)}
//                                                             className={
//                                                                 currentSelectIndex === index
//                                                                     ?
//                                                                     'mds-executePage-liWrap mds-executePage-liCurrentSelect'
//                                                                     :
//                                                                     (
//                                                                         item.isSelect === 'no'
//                                                                             ?
//                                                                             'mds-executePage-liWrap mds-executePage-liNoSelect'
//                                                                             :
//                                                                             'mds-executePage-liWrap mds-executePage-liSelect'
//                                                                     )

//                                                             }
//                                                         >
//                                                             <span className='mds-executePage-liName'>{item.tableName}</span>
//                                                         </li>
//                                                     </Tooltip>
//                                                 )
//                                             })
//                                             :
//                                             <Empty style={{ marginTop: '35px' }} />
//                                     }
//                                 </ul>
//                             </div>
//                         </Spin>
//                     </div>

//                     <div className='mds-executePage-btns' >
//                         <MyButton
//                             label='上一步'
//                             onClick={this.btnClick.bind(this, 'pre')}
//                             style={{ margin: '0 40px' }}
//                         />
//                         <MyButton
//                             label='返回'
//                             onClick={() => this.btnClick('cancel')}
//                             classType='warm'
//                         />
//                     </div>
//                 </div>
// <Modal
//     title="定义标签"
//     visible={visible}
//     onCancel={this.modalCancel}
//     destroyOnClose
//     footer={
//         <Fragment>
//             <Button type={'primary'} onClick={this.modalOk.bind(this, '1')}>确定</Button>
//             <MyButton label='取消' classType='warm' onClick={this.modalCancel} />
//         </Fragment>
//     }
// >
//     <div className='mds-LabelDefinition-modalBody'>
//         <p>文本：湖北</p>
//         <div className='mds-LabelDefinition-modalConditionWrap'>
//             <Input.Group compact style={{ display: 'flex' }}>
//                 <Input onChange={this.debounceSearchInput.bind(this, 'search1')} />
//                 <ColorPicker
//                     animation="slide-up"
//                     defaultColor={'#1890FF'}
//                     onChange={this.colorChange}
//                 />
//             </Input.Group>
//             {
//                 radioList.length > 0
//                     ?
//                     <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={this.radioChange.bind(this)} value={radioValue}>
//                         {
//                             radioList.map(item => {
//                                 return (
//                                     <Radio style={{ color: item.color }} value={item.id}>{item.text}</Radio>
//                                 )
//                             })
//                         }
//                     </Radio.Group>
//                     :
//                     <div style={{ textAlign: 'center', marginTop: '15px' }}>
//                         <Empty />
//                     </div>

//             }
//         </div>
//     </div>
// </Modal>
// <Modal
//     title="定义标签"
//     visible={visible2}
//     onCancel={this.modalCancel}
//     destroyOnClose
//     footer={
//         <Fragment>
//             <Button type={'primary'} onClick={this.modalOk.bind(this, '2')}>确定</Button>
//             <MyButton label='取消' classType='warm' onClick={this.modalCancel} />
//         </Fragment>
//     }
// >
//     <div className='mds-LabelDefinition-modalBody'>
//         <p>from：{fromLabel}</p>
//         <p>to：{endLabel}</p>
//         <div className='mds-LabelDefinition-modalConditionWrap'>
//             <Input.Group compact style={{ display: 'flex' }}>
//                 <Input onChange={this.debounceSearchInput.bind(this, 'search2')} />
//                 <ColorPicker
//                     animation="slide-up"
//                     defaultColor={'#1890FF'}
//                     onChange={this.colorChange}
//                 />
//             </Input.Group>
//             {
//                 radioConnectionList.length > 0
//                     ?
//                     <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={this.radioChange.bind(this)} value={radioValue}>
//                         {
//                             radioConnectionList.map(item => {
//                                 return (
//                                     <Radio value={item.id}>{item.text}</Radio>
//                                 )
//                             })
//                         }
//                     </Radio.Group>
//                     :
//                     <div style={{ textAlign: 'center', marginTop: '15px' }}>
//                         <Empty />
//                     </div>
//             }
//         </div>
//     </div>
// </Modal>
//             </div>
//         )
//     }
// }