import React, { useEffect, useState, Fragment } from 'react'
import LayoutHeader from '@/components/Header'
import './index.less'
import '../../TaskManagement/ConfirmGuideLinePage/index.less'
import * as api from './services'
import { Select, Button, Spin, Pagination, Empty, Drawer, Tabs, Tooltip, Input, Modal, Radio, message } from 'antd'
import { Annotator, Action } from 'poplar-annotation';
import { MyButton } from '@/components/commonWrap';
import MyModal from '@/components/Modal'
import $ from 'jquery'

const { Option } = Select;
const { TabPane } = Tabs;

export default function Index(props) {

    const [currentSelectIndex, setCurrentSelectIndex] = useState(0)
    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageTotal, setPageTotal] = useState(0)
    const [pageCurrent2, setPageCurrent2] = useState(1)
    const [pageSize2, setPageSize2] = useState(10)
    const [pageTotal2, setPageTotal2] = useState(0)
    const [svgList, setSvgList] = useState([])
    const [svgSpinning, setSvgSpinning] = useState(true)
    const [spinning, setSpinning] = useState(true)
    const [labelStateTypeSel, setLabelStateTypeSel] = useState('all')
    const [taskId, setTaskId] = useState(null)
    const [drawerVisible, setDrawerVisible] = useState(false);
    const [schemaList, setSchemaList] = useState([])
    const [tag, setTag] = useState('01')
    const [schemaId, setSchemaId] = useState(null)
    const [definition, setDefinition] = useState('')
    const [tabPaneKey, setTabPaneKey] = useState('1')
    const [liList, setLiList] = useState([])
    const [labelTypeList, setLabelTypeList] = useState([])
    const [keyWord2, setKeyWord2] = useState(null)
    const [typeCode, setTypeCode] = useState(null)
    const [tabSpin, setTabSpin] = useState(true)
    const [svgAllInfo, setSvgAllInfo] = useState({})
    const [currentSvg, setCurrentSvg] = useState(null)
    const [rowIndex, setRowIndex] = useState(null)
    const [startIndex, setStartIndex] = useState(null)
    const [endIndex, setEndIndex] = useState(null)
    const [seletText, setSeletText] = useState(null)
    const [visible, setVisible] = useState(false)
    const [visible2, setVisible2] = useState(false)
    const [first, setFirst] = useState(null)
    const [second, setSecond] = useState(null)
    const [fromLabel, setFromLabel] = useState(null)
    const [endLabel, setEndLabel] = useState(null)
    const [attrMappingId, setAttrMappingId] = useState(null)
    const [evalId, setEvalId] = useState(null)
    const [isMark, setIsMark] = useState('all')
    const [radioValue, setRadioValue] = useState(null)
    const [radioConnectionList, setRadioConnectionList] = useState([])
    const [radioList, setRadioList] = useState([])
    const [labelCategories, setLabelCategories] = useState([])
    const [connectionCategories, setConnectionCategories] = useState([])
    const [myModalVisible, setMyModalVisible] = useState(false)
    const [modalTitle, setModalTitle] = useState('')
    const [publishState, setPublishState] = useState('')
    const [modalContent, setModalContent] = useState('')




    useEffect(() => {
        const { taskId } = props.match.params
        setTaskId(taskId)
    }, [])

    useEffect(() => {
        if (taskId && pageCurrent && isMark && pageSize) {
            getMainSvgList()
        }
    }, [taskId, pageCurrent, isMark, pageSize])

    useEffect(() => {
        if (drawerVisible) {
            setTimeout(() => {
                getLabelTypeList()
            }, 500);

        }
    }, [drawerVisible])

    useEffect(() => {
        if (typeCode) {
            getLiList()
        }
    }, [typeCode])

    useEffect(() => {
        if (schemaId && pageCurrent2 && tag) {
            getDrawerSchemaList()
        }
    }, [pageCurrent2, schemaId, tag])

    // 获取主要svg
    const getMainSvgList = () => {
        api.getMainSvgList({
            pageSize,
            pageCount: pageCurrent,
            isMark,
            taskId
        }).then(res => {
            const temp1 = JSON.parse(JSON.stringify(res.labelCategories))
            const temp2 = JSON.parse(JSON.stringify(res.connectionCategories))
            setLabelCategories(temp1)
            setRadioList(temp1)
            setRadioConnectionList(temp2)
            setConnectionCategories(temp2)
            setSvgList(res.data)
            setSvgSpinning(false)
            setPageTotal(res.totalCount)
        }).catch(err => {
            setSvgSpinning(false)
        })
    }

    // 获取标签分类
    const getLabelTypeList = () => {
        api.getLabelTypeList({
            taskId
        }).then(res => {
            setLabelTypeList(res.data)
            setTypeCode(res.data.length > 0 ? res.data[0].code : null)
        }).catch(err => { })
    }

    // 获取抽屉左侧列表
    const getLiList = (type) => {
        api.getLiList({
            taskId,
            typeCode,
            keyWord: keyWord2
        }).then(res => {
            setLiList(res.data)
            setSchemaId(res.data.length > 0 ? res.data[0].schemaId : null)
            setDefinition(res.data.length > 0 ? res.data[0].definition : null)
            setSpinning(false)
        }).catch(err => { })
    }

    // 获取抽屉右侧列表
    const getDrawerSchemaList = (type) => {
        api.getDrawerSchemaList({
            schemaId,
            tag,
            pageSize: pageSize2,
            pageCount: pageCurrent2
        }).then(res => {
            setSchemaList(res.data)
            setPageTotal2(res.totalCount)
            setTabSpin(false)
        }).catch(err => { })
    }


    // 显示抽屉
    const showDrawer = () => {
        setDrawerVisible(true);
    };

    // 关闭抽屉
    const onClose = () => {
        setDrawerVisible(false)
        setCurrentSelectIndex(0)
        setTag('01')
        setTabPaneKey('1')
        setTypeCode(null)
        setSchemaId(null)
        $('.svgContainer').remove()
        setSchemaList([])
        setTabSpin(true)
        setSpinning(true)
    };



    // tabs切换事件
    const tabChange = (value) => {
        if (tabPaneKey !== value) {
            $('.svgContainer').remove()
            setSchemaList([])
            setPageCurrent2(1)
            setTabPaneKey(value)
            setTag(value === '1' ? '01' : '02')
            setTabSpin(true)
        }
    }


    // 弹窗取消
    const modalCancel = () => {
        setVisible(false)
        setVisible2(false)
    }

    const radioChange = ({ target: { value } }) => {
        setRadioValue(value)
    }

    // 渲染SVG
    const renderMainSvg = (item, index) => {
        const len = $("#svgMainAllContainer").children("#svgMainContainer" + index).length
        if (len === 0) {
            $('#svgMainAllContainer').append("<div class='svgMainContainer' id='svgMainContainer" + index + "'></div>")
        }
        let svgDom = document.getElementById('svgMainContainer' + index)
        const tempArry = JSON.parse(JSON.stringify(item.annotation.labelCategories))
        const tempArry2 = JSON.parse(JSON.stringify(item.annotation.connectionCategories))
        // const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
        // const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
        if (svgDom && $('#svgMainContainer' + index).children().length === 0) {
            let annotator = new Annotator({
                content: item.annotation.content,
                labelCategories: tempArry,
                labels: item.annotation.labels,
                connectionCategories: tempArry2,
                connections: item.annotation.connections,
            }, document.getElementById('svgMainContainer' + index), {
                labelWidthCalcMethod: 'max'
            })
            annotator.on('textSelected', (startIndex, endIndex) => {
                let flag = false
                const labels = svgAllInfo[index].store.json.labels
                const len = labels.length
                for (let index2 = 0; index2 < len; index2++) {
                    if (labels[index2].startIndex === startIndex && labels[index2].endIndex === endIndex) {
                        message.error('请勿重复添加实体！')
                        flag = true
                        break;
                    }

                }
                // 输出用户选取的那些字
                // let userChoosedCategoryId = getUserChoosedCategoryId();
                // annotator.applyAction(Action.Label.Create(1, startIndex, endIndex))
                if (!flag) {
                    let label = svgAllInfo[index].store.json.content.substring(startIndex, endIndex)
                    setVisible(true)
                    setCurrentSvg(index)
                    setRowIndex(item.rowIndex)
                    setAttrMappingId(item.attrMappingId)
                    setEvalId(item.evalId)
                    setStartIndex(startIndex)
                    setEndIndex(endIndex)
                    setSeletText(label)
                }
            });
            annotator.on('twoLabelsClicked', (first, second) => {
                let fromLabel = ''
                let endLabel = ''
                let startIndex = ''
                let endIndex = ''
                let flag = false
                const connections = svgAllInfo[index].store.json.connections
                const len = connections.length
                for (let index2 = 0; index2 < len; index2++) {
                    if (connections[index2].fromId === first && connections[index2].toId === second) {
                        message.error('请勿重复添加相同关系！')
                        flag = true
                        break;
                    }

                }
                if (!flag) {
                    if (svgAllInfo[index]) {
                        svgAllInfo[index].store.json.labels.map(item => {
                            if (first === second) {
                                if (item.id === first) {
                                    fromLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                    endLabel = fromLabel
                                    startIndex = item.startIndex + "--AA--" + item.endIndex
                                    endIndex = startIndex
                                }
                            } else {
                                if (item.id === first) {
                                    fromLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                    startIndex = item.startIndex + "--AA--" + item.endIndex
                                } else if (item.id === second) {
                                    endLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                    endIndex = item.startIndex + "--AA--" + item.endIndex
                                }
                            }
                        })
                    }
                    setAttrMappingId(item.attrMappingId)
                    setEvalId(item.evalId)
                    setVisible2(true)
                    setCurrentSvg(index)
                    setRowIndex(item.rowIndex)
                    setStartIndex(startIndex)
                    setEndIndex(endIndex)
                    setFirst(first)
                    setSecond(second)
                    setFromLabel(fromLabel)
                    setEndLabel(endLabel)
                }
            })
            annotator.on('connectionRightClicked', (id, event) => {
                Modal.confirm({
                    title: "确认删除吗？",
                    onOk() {
                        let params = {}
                        svgAllInfo[index].store.json.connections.map(obj => {
                            if (obj.id === id) {
                                const labels = svgAllInfo[index].store.json.labels
                                const len = labels.length
                                let startIndex, endIndex
                                for (let idx = 0; idx < len; idx++) {
                                    if (startIndex && endIndex) {
                                        break;
                                    } else if (labels[idx].id === obj.fromId) {
                                        startIndex = labels[idx].startIndex + "--AA--" + labels[idx].endIndex
                                    } else if (labels[idx].id === obj.toId) {
                                        endIndex = labels[idx].startIndex + "--AA--" + labels[idx].endIndex
                                    }
                                }
                                params['attrMappingId'] = item.attrMappingId
                                params['rowIndex'] = item.rowIndex
                                params['labelId'] = obj.categoryId
                                params['fromId'] = startIndex
                                params['toId'] = endIndex
                                params['evalId'] = item.evalId
                            }
                        })
                        api.delSvgInfo([params]).then(res => {
                            svgAllInfo[index].applyAction(Action.Connection.Delete(id))
                            api.saveSvgInfo({
                                attrMappingId: item.attrMappingId,
                                rowIndex: item.rowIndex,
                                evalId: item.evalId,
                                annotation: svgAllInfo[index].store.json
                            }, taskId).then(res => {
                                // getMainSvgList()
                            }).catch(err => { })
                        }).catch(err => { })
                    },
                    cancelButtonProps: {
                        className: 'mds-btn-warm',
                        style: {
                            marginLeft: '8px'
                        }
                    }
                })
            });
            annotator.on('labelRightClicked', (id, e) => {
                Modal.confirm({
                    title: "确认删除吗？",
                    onOk() {
                        let params = []
                        let flag = false
                        svgAllInfo[index].store.json.connections.map(item2 => {
                            if (item2.fromId === id || item2.toId === id) {
                                flag = true
                            }
                        })
                        if (flag) {
                            message.error('当前实体含有关系，请先删除相关关系！')
                        } else {
                            const obj1 = svgAllInfo[index].store.json.labels
                            const len1 = obj1.length
                            for (let index2 = 0; index2 < len1; index2++) {
                                if (obj1[index2].id === id) {
                                    params.push({
                                        attrMappingId: item.attrMappingId,
                                        rowIndex: item.rowIndex,
                                        labelId: obj1[index2].categoryId,
                                        fromId: obj1[index2].startIndex,
                                        toId: obj1[index2].endIndex,
                                        evalId: item.evalId
                                    })
                                    break;
                                }
                            }

                            api.delSvgInfo(params).then(res => {
                                svgAllInfo[index].applyAction(Action.Label.Delete(id))
                                api.saveSvgInfo({
                                    attrMappingId: item.attrMappingId,
                                    rowIndex: item.rowIndex,
                                    evalId: item.evalId,
                                    annotation: svgAllInfo[index].store.json
                                }, taskId).then(res => {
                                    // getMainSvgList()
                                }).catch(err => { })
                            }).catch(err => { })
                        }
                    },
                    cancelButtonProps: {
                        className: 'mds-btn-warm',
                        style: {
                            marginLeft: '8px'
                        }
                    }
                })
            })
            svgAllInfo[index] = annotator
            setSvgAllInfo(svgAllInfo)
        }
    }

    // 翻页事件
    function pageChange(page, size, type) {
        if (type === '2') {
            $('.svgContainer').remove()
            setTabSpin(false)
            setSchemaList([])
            setPageCurrent2(page)
        } else {
            $('.svgMainContainer').remove()
            setSvgAllInfo({})
            setSvgList([])
            setSvgSpinning(true)
            setPageCurrent(page)
            setPageSize(size)
        }
    }

    function modalOk(type) {
        if (type === '1') {
            if (radioValue !== '' && radioValue !== undefined && radioValue !== null) {
                api.saveMakeLabel({
                    attrMappingId,
                    rowIndex,
                    labelId: radioValue,
                    fromId: startIndex,
                    toId: endIndex,
                    markFileName: '',
                    tag: '',
                    evalId
                }).then(res => {
                    svgAllInfo[currentSvg].applyAction(Action.Label.Create(radioValue, startIndex, endIndex))
                    setVisible(false)
                    setRadioValue(null)
                }).then(res => {
                    api.saveSvgInfo({
                        attrMappingId,
                        rowIndex,
                        evalId,
                        annotation: svgAllInfo[currentSvg].store.json
                    }, taskId).then(res => {
                        // getMainSvgList()
                    }).catch(err => { })
                })
            } else {
                message.error('请选择标签！')
            }
        } else if (type === '2') {
            if (radioValue !== '' && radioValue !== undefined && radioValue !== null) {
                api.saveMakeLabel({
                    attrMappingId,
                    rowIndex,
                    labelId: radioValue,
                    fromId: startIndex,
                    toId: endIndex,
                    markFileName: '',
                    tag: '',
                    evalId
                }).then(res => {
                    svgAllInfo[currentSvg].applyAction(Action.Connection.Create(radioValue, first, second))
                    setVisible2(false)
                    setRadioValue(null)
                }).then(res => {
                    api.saveSvgInfo({
                        attrMappingId,
                        rowIndex,
                        evalId,
                        annotation: svgAllInfo[currentSvg].store.json
                    }, taskId).then(res => {
                        // getMainSvgList()
                    }).catch(err => { })
                })
            } else {
                message.error('请选择关系！')
            }
        } else if (type === 'confirm') {
            api.confirmSubmit({
                taskId
            }).then(res => {
                message.success('提交成功,窗口即将关闭！')
                window.opener.location.reload();
                setTimeout(() => {
                    window.close()
                }, 500);
            }).catch(err => { })
        }
    }

    function inputChange({ target: { value } }, type) {
        if (type === 'keyWord2') {
            setKeyWord2(value)
        }
    }

    // 渲染SVG
    function renderSvg(item, index) {
        const wrap = tag === '01' ? "#svgAllContainer1" : "#svgAllContainer2"
        const len = $(wrap).children("#svgContainer" + index).length
        if (len === 0) {
            $(wrap).append("<div class='svgContainer' id='svgContainer" + index + "'></div>")
        }
        let svgDom = document.getElementById('svgContainer' + index)
        // const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
        // const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
        if (svgDom && $('#svgContainer' + index).children().length === 0) {
            let annotator = new Annotator(item.annotation, document.getElementById('svgContainer' + index), {
                labelWidthCalcMethod: 'max'
            })
        }
    }

    // 按钮时间
    function btnClick(type) {
        if (type === 'lookGuideLine') {
            setDrawerVisible(true)
        } else if (type === 'finish') {
            api.getModalInfo({
                taskId
            }).then(res => {
                const { data: { publishComfirm, publishState } } = res
                setModalTitle('提示')
                setPublishState(publishState)
                setMyModalVisible(true)
                setModalContent(publishComfirm)
            }).catch(err => { })
        } else if (type === 'cancel') {
            window.close()
        }
    }

    function liClick(item, index) {
        if (currentSelectIndex !== index) {
            const { definition, schemaId } = item
            $('.svgContainer').remove();
            setCurrentSelectIndex(index)
            setSchemaId(schemaId)
            setTag('01')
            setTabPaneKey('1')
            setSchemaList([])
            setDefinition(definition)
            setTabSpin(true)
        }
    }

    function selChange(value, type) {
        if (type === 'typeCode') {
            setTypeCode(value)
            $('.svgContainer').remove();
            setCurrentSelectIndex(0)
            setSchemaList([])
        } else if (type === 'isMark') {
            $('.svgMainContainer').remove()
            setSvgList([])
            setSvgAllInfo({})
            setSvgSpinning(true)
            setPageCurrent(1)
            setIsMark(value)
        }
    }

    function inputSearch(value, type) {
        if (type === 'label') {
            let tempLabelArry
            if (value) {
                tempLabelArry = labelCategories.filter(item => item.text.indexOf(value) > -1)
            } else {
                tempLabelArry = JSON.parse(JSON.stringify(labelCategories))
            }
            setRadioList(tempLabelArry)
            setRadioValue(null)
        } else {
            let tempLabelArry
            if (value) {
                tempLabelArry = connectionCategories.filter(item => item.text.indexOf(value) > -1)
            } else {
                tempLabelArry = JSON.parse(JSON.stringify(connectionCategories))
            }
            setRadioConnectionList(tempLabelArry)
            setRadioValue(null)
        }
    }

    return (
        <div className='mds-all-layout-wrap' >
            <LayoutHeader />
            <div className='mds-executePage-wrap'>
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <div className='mds-usMarkingTraining-bodyWrap'>
                        <div className='mds-usMarkingTraining-headWrap'>
                            <Select onChange={(value) => selChange(value, 'isMark')} value={isMark} style={{ width: '200px' }}>
                                <Option value='all'>全部</Option>
                                <Option value='01'>已标注</Option>
                                <Option value='02'>未标注</Option>
                            </Select>
                            <Button type='primary' onClick={() => btnClick('lookGuideLine')}>查看GUIDELINE</Button>
                        </div>
                        <div className='mds-usMarkingTraining-middleWrap'>
                            <Spin style={{ height: '100%' }} spinning={svgSpinning}>
                                <div id='svgMainAllContainer'>
                                    {
                                        svgList.length > 0
                                            ?
                                            svgList.map((item, index) => {
                                                return (
                                                    renderMainSvg(item, index)
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </div>
                            </Spin>
                        </div>
                        <Pagination
                            className='mds-executePage-svgPageNation'
                            current={pageCurrent}
                            pageSize={pageSize}
                            onChange={(page, pageSize) => pageChange(page, pageSize, '1')}
                            total={pageTotal}
                        />
                        <div className='mds-usMarkingTraining-btns'>
                            <MyButton
                                label='完成'
                                onClick={() => btnClick('finish')}
                                style={{ marginRight: '40px' }}
                            />
                            <MyButton
                                label='取消'
                                onClick={() => btnClick('cancel')}
                                classType='warm'
                            />
                        </div>
                    </div>
                </div>
            </div>
            <Drawer
                title="查看GUIDELINE"
                maskClosable={false}
                placement="right"
                closable={true}
                onClose={onClose}
                destroyOnClose={true}
                visible={drawerVisible}
                width='80%'
            >
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Select value={typeCode} onChange={(value) => selChange(value, 'typeCode')} placeholder='请选择' style={{ marginRight: '5px' }}>
                                    {
                                        labelTypeList.length > 0
                                            ?
                                            labelTypeList.map(item => {
                                                return (
                                                    <Option value={item.code}>{item.value}</Option>
                                                )
                                            })
                                            :
                                            null
                                    }
                                </Select>
                                <Input onChange={(e) => inputChange(e, 'keyWord2')} onPressEnter={() => {
                                    setCurrentSelectIndex(null)
                                    $('.svgContainer').remove();
                                    setCurrentSelectIndex(0)
                                    setSchemaId(null)
                                    setPageCurrent2(1)
                                    setSchemaList([])
                                    getLiList()
                                }} placeholder='请输入标签名' />
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                {
                                    liList.length > 0
                                        ?
                                        liList.map((item, index) => {
                                            return (
                                                <li
                                                    key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                    onClick={() => liClick(item, index)}
                                                    className={
                                                        currentSelectIndex === index
                                                            ?
                                                            'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                            :
                                                            (
                                                                item.isSelect === 'no'
                                                                    ?
                                                                    'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                    :
                                                                    'mds-executePage-liWrap mds-executePage-liSelect'
                                                            )
                                                    }
                                                >
                                                    <span className='mds-executePage-liName'>{item.schemaTip}</span>
                                                </li>
                                            )
                                        })
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
                            </ul>
                        </div>
                    </Spin>
                    <div className='mds-executePage-middleWrap' style={{ padding: '20px', }}>
                        <div className='mds-guideLine-head'>
                            <p>标签定义</p>
                            <div className='mds-guideLine-headContent'>
                                <Tooltip title={definition}>
                                    {definition}
                                </Tooltip>
                            </div>
                            <div className='mds-guideLine-tabWrap'>
                                <Tabs activeKey={tabPaneKey} onChange={tabChange}>
                                    <TabPane tab="正面例子" key="1">
                                        <Spin spinning={tabSpin}>
                                            <div id='svgAllContainer1'>
                                                {
                                                    schemaList.length > 0
                                                        ?
                                                        schemaList.map((item, index) => {
                                                            return (
                                                                renderSvg(item, index)
                                                            )
                                                        })
                                                        :
                                                        <Empty />
                                                }
                                            </div>
                                            <Pagination
                                                className='mds-executePage-svgPageNation'
                                                current={pageCurrent2}
                                                showQuickJumper
                                                pageSize={pageSize2}
                                                onChange={(page, pageSize) => pageChange(page, pageSize, '2')}
                                                total={pageTotal2}

                                            />
                                        </Spin>
                                    </TabPane>
                                    <TabPane tab="负面例子" key="2">
                                        <Spin spinning={tabSpin}>
                                            <div id='svgAllContainer2'>
                                                {
                                                    schemaList.length > 0
                                                        ?
                                                        schemaList.map((item, index) => {
                                                            return (
                                                                renderSvg(item, index)
                                                            )
                                                        })
                                                        :
                                                        <Empty />
                                                }
                                            </div>
                                            <Pagination
                                                className='mds-executePage-svgPageNation'
                                                current={pageCurrent2}
                                                showQuickJumper
                                                pageSize={pageSize2}
                                                onChange={(page, pageSize) => pageChange(page, pageSize, '2')}
                                                total={pageTotal2}
                                            />
                                        </Spin>
                                    </TabPane>
                                </Tabs>
                            </div>
                        </div>
                    </div>
                </div>
            </Drawer>
            <Modal
                title="定义标签"
                visible={visible}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('1')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <div className='mds-LabelDefinition-modalBody'>
                    <p>文本：{seletText}</p>
                    <div className='mds-LabelDefinition-modalConditionWrap'>
                        <p>选择已有标签：</p>
                        <Input.Search placeholder="输入搜索标签" onSearch={value => inputSearch(value, 'label')} enterButton />
                        {
                            radioList.length > 0
                                ?
                                <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={radioChange} value={radioValue}>
                                    {
                                        radioList.map(item => {
                                            if (item.id !== 'originId') {
                                                return (
                                                    <Radio style={{ color: item.color }} value={item.id}>{item.text}</Radio>
                                                )
                                            }
                                        })
                                    }
                                </Radio.Group>
                                :
                                <div style={{ textAlign: 'center', marginTop: '15px' }}>
                                    <Empty />
                                </div>

                        }
                    </div>
                </div>
            </Modal>
            <Modal
                title="定义标签"
                visible={visible2}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('2')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <div className='mds-LabelDefinition-modalBody'>
                    <p>from：{fromLabel}</p>
                    <p>to：{endLabel}</p>
                    <div className='mds-LabelDefinition-modalConditionWrap'>
                        <p>选择已有关系：</p>
                        <Input.Search placeholder="输入搜索关系" onSearch={value => inputSearch(value, 'connection')} enterButton />
                        {
                            radioConnectionList.length > 0
                                ?
                                <Radio.Group className='mds-LabelDefinition-radioWrap' onChange={radioChange} value={radioValue}>
                                    {
                                        radioConnectionList.map(item => {
                                            return (
                                                <Radio value={item.id}>{item.text}</Radio>
                                            )
                                        })
                                    }
                                </Radio.Group>
                                :
                                <div style={{ textAlign: 'center', marginTop: '15px' }}>
                                    <Empty />
                                </div>
                        }
                    </div>
                </div>
            </Modal>

            <MyModal
                visible={myModalVisible}
                // modalOk={modalOk}
                modalCancel={() => {
                    setMyModalVisible(false)
                }}
                title={modalTitle}
                footer={
                    publishState === '01'
                        ?
                        <Button type={'primary'} onClick={() => {
                            setMyModalVisible(false)
                        }}>
                            {'确定'}
                        </Button>
                        :
                        <Fragment>
                            <Button type={'primary'} onClick={() => modalOk('confirm')}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={() => {
                                setMyModalVisible(false)
                            }} />
                        </Fragment>

                }
            >
                <Fragment>
                    <p dangerouslySetInnerHTML={{ __html: modalContent }}></p>
                </Fragment>
            </MyModal>
        </div>
    )
}
