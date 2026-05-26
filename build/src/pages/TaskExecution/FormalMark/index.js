import React, { useEffect, useState, Fragment, useCallback } from 'react'
import { MenuFoldOutlined } from '@ant-design/icons';
import LayoutHeader from '@/components/Header';
import { Annotator, Action } from 'poplar-annotation';
import { Select, Spin, Tooltip, Empty, Input, Modal, Button, message, Radio, Pagination, Space, Drawer, Tabs } from 'antd';
import { MyButton } from '@/components/commonWrap';
import * as api from './services'
import $ from 'jquery'
import MyModal from '@/components/Modal'
import './index.less'

const { Option } = Select
const { TabPane } = Tabs



export default function Index(props) {

    const [liList, setLiList] = useState([])
    const [fieldList, setFieldList] = useState([])
    const [schemaList, setSchemaList] = useState([])
    const [svgList, setSvgList] = useState([])
    const [drawerLiList, setDrawerLiList] = useState([])
    const [labelTypeList, setLabelTypeList] = useState([])
    const [typeCode, setTypeCode] = useState(null)
    const [tag, setTag] = useState('01')
    const [isMark, setIsMark] = useState('all')
    const [isMark2, setIsMark2] = useState('all')
    const [keyWord, setKeyWord] = useState('')
    const [keyWord2, setKeyWord2] = useState('')
    const [liLevel, setLiLevel] = useState('level1')
    const [tabPaneKey, setTabPaneKey] = useState('1')
    const [definition, setDefinition] = useState(null)
    const [currentSelectIndex, setCurrentSelectIndex] = useState(null)
    const [currentSelectFieldIndex, setCurrentSelectFieldIndex] = useState(null)
    const [drawerSelectIndex, setDrawerSelectIndex] = useState(null)
    const [svgSpinning, setSvgSpinning] = useState(false)
    const [tabSpin, setTabSpin] = useState(true)
    const [spinning, setSpinning] = useState(true)
    const [spinning2, setSpinning2] = useState(true)
    const [drawerVisible, setDrawerVisible] = useState(false)
    const [myModalVisible, setMyModalVisible] = useState(false)
    const [modalContent, setModalContent] = useState('')
    const [publishState, setPublishState] = useState('')
    const [pageSize, setPageSize] = useState(10)
    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageCount, setPageCount] = useState(0)
    const [drawerPageSize, setDrawerPageSize] = useState(5)
    const [drawerPageCurrent, setDrawerPageCurrent] = useState(1)
    const [drawerPageCount, setDrawerPageCount] = useState(0)
    const [taskId, setTaskId] = useState(null)
    const [tableOrigin, setTableOrigin] = useState('')
    const [tableMappingId, setTableMappingId] = useState(null)
    const [attrMappingId, setAttrMappingId] = useState(null)
    const [columnName, setColumnName] = useState(null)
    const [schemaId, setSchemaId] = useState(null)
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
    const [evalId, setEvalId] = useState(null)
    const [radioValue, setRadioValue] = useState(null)
    const [radioConnectionList, setRadioConnectionList] = useState([])
    const [radioList, setRadioList] = useState([])
    const [labelCategories, setLabelCategories] = useState([])
    const [connectionCategories, setConnectionCategories] = useState([])
    let timeout;

    useEffect(() => {
        const { taskId } = props.match.params
        setTaskId(taskId)
    }, [])

    useEffect(() => {
        if ((liLevel === 'level1' && taskId && isMark)) {
            getTableList()
        } else if ((liLevel === 'level2' && taskId && isMark)) {
            getFieldList()
        }
    }, [taskId, isMark, isMark, liLevel])

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
        if (schemaId) {
            getDrawerSvgList()
        }
    }, [schemaId, tag, drawerPageCurrent])

    useEffect(() => {
        if (attrMappingId && pageCurrent && isMark2) {
            getSvgList()
        }
    }, [attrMappingId, pageCurrent, isMark2, pageSize])

    // 获取标签分类
    const getLabelTypeList = () => {
        api.getLabelTypeList({
            taskId
        }).then(res => {
            setLabelTypeList(res.data)
            setTypeCode(res.data.length > 0 ? res.data[0].code : null)
        }).catch(err => { })
    }

    // 获取level1表列表
    const getTableList = () => {
        api.getTableList({
            taskId,
            keyWord,
            isMark
        }).then(res => {
            setSpinning(false)
            setLiList(res.data)
        }).catch(err => {
            setSpinning(false)
        })
    }

    // 获取level2表下面的字段列表
    const getFieldList = () => {
        api.getFieldList({
            tableMappingId,
            isMark,
            taskId,
            keyWord
        }).then(res => {
            setSpinning(false)
            setFieldList(res.data)
        }).catch(err => {
            setSpinning(false)
        })
    }

    // drawer左侧列表
    const getLiList = () => {
        api.getLiList({
            taskId,
            typeCode,
            keyWord: keyWord2
        }).then(res => {
            setDrawerLiList(res.data)
            setSchemaId(res.data.length > 0 ? res.data[0].schemaId : null)
            setDefinition(res.data.length > 0 ? res.data[0].definition : null)
            setDrawerSelectIndex(res.data.length > 0 ? 0 : null)
            setSpinning2(false)
        }).catch(err => {
            setSpinning2(false)
        })
    }

    // drawer SVG列表
    const getDrawerSvgList = () => {
        api.getDrawerSvgList({
            schemaId,
            tag,
            pageCount: drawerPageCurrent,
            pageSize: drawerPageSize
        }).then(res => {
            setSchemaList(res.data)
            setDrawerPageCount(res.totalCount)
            setTabSpin(false)
        }).catch(err => {
            setTabSpin(false)
        })
    }

    // 获取svg列表
    const getSvgList = () => {
        api.getSvgList({
            tableMappingId,
            tableName: tableOrigin,
            attrMappingId,
            columnName,
            isMark: isMark2,
            taskId,
            pageSize,
            pageCount: pageCurrent
            // keyWord
        }).then(res => {
            const temp1 = JSON.parse(JSON.stringify(res.labelCategories))
            const temp2 = JSON.parse(JSON.stringify(res.connectionCategories))
            setLabelCategories(temp1)
            setRadioList(temp1)
            setRadioConnectionList(temp2)
            setConnectionCategories(temp2)
            setSvgSpinning(false)
            setSvgList(res.data)
            setPageCount(res.totalCount)
        }).catch(err => {
            setSpinning(false)
        })
    }

    // tabs切换事件
    const tabChange = (value) => {
        if (tabPaneKey !== value) {
            $('.svgContainer').remove()
            setSchemaList([])
            setDrawerPageCurrent(1)
            setTabPaneKey(value)
            setTag(value === '1' ? '01' : '02')
            setTabSpin(true)
        }
    }

    // drawer关闭事件
    const onClose = () => {
        $('.svgContainer').remove()
        setDrawerVisible(false)
        setSpinning2(true)
        setTabSpin(true)
        setDrawerLiList([])
        setDrawerSelectIndex(null)
        setSchemaList([])
        setTag('01')
        setTabPaneKey('1')
        setTypeCode(null)
        setSchemaId(null)
        setKeyWord2(null)
    }

    // 弹窗取消
    const modalCancel = () => {
        setVisible(false)
        setVisible2(false)
    }

    // 渲染SVG
    const renderSvg = (item, index) => {
        const wrap = tag === '01' ? "#svgAllContainer1" : "#svgAllContainer2"
        const len = $(wrap).children("#svgContainer" + index).length
        if (len === 0) {
            $(wrap).append("<div class='svgContainer' id='svgContainer" + index + "'></div>")
        }
        let svgDom = document.getElementById('svgContainer' + index)
        // const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
        // const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
        if (svgDom && $('#svgContainer' + index).children().length === 0) {
            new Annotator(item.annotation, document.getElementById('svgContainer' + index), {
                labelWidthCalcMethod: 'max',
                unconnectedLineStyle: 'none'
            })
        }
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
                                }
                            } else {
                                if (item.id === first) {
                                    fromLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                } else if (item.id === second) {
                                    endLabel = svgAllInfo[index].store.json.content.substring(item.startIndex, item.endIndex)
                                }
                            }
                        })
                    }
                    setAttrMappingId(item.attrMappingId)
                    setEvalId(item.evalId)
                    setVisible2(true)
                    setCurrentSvg(index)
                    setRowIndex(item.rowIndex)
                    setStartIndex(first)
                    setEndIndex(second)
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
                        // let params = {}
                        // svgAllInfo[index].store.json.connections.map(obj => {
                        //     if (obj.id === id) {
                        //         params['attrMappingId'] = item.attrMappingId
                        //         params['rowIndex'] = item.rowIndex
                        //         params['labelId'] = obj.categoryId
                        //         params['fromId'] = obj.fromId
                        //         params['toId'] = obj.toId
                        //         params['evalId'] = item.evalId
                        //     }
                        // })
                        annotator.applyAction(Action.Connection.Delete(id))
                        api.saveSvgInfo({
                            attrMappingId: item.attrMappingId,
                            rowIndex: item.rowIndex,
                            evalId: item.evalId,
                            annotation: annotator.store.json
                        }, taskId).then(res => {
                            // getSvgList()
                            setVisible(false)
                            setRadioValue(null)
                        }).catch(err => { })
                        // api.delSvgInfo([params]).then(res => {
                        //     svgAllInfo[index].applyAction(Action.Connection.Delete(id))
                        //     api.saveSvgInfo({
                        //         attrMappingId: item.attrMappingId,
                        //         rowIndex: item.rowIndex,
                        //         evalId: item.evalId,
                        //         annotation: svgAllInfo[index].store.json
                        //     }, taskId).then(res => {
                        //         getMainSvgList()
                        //     }).catch(err => { })
                        // }).catch(err => { })
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
                debugger
                Modal.confirm({
                    title: "确认删除吗？",
                    onOk() {
                        // let params = []
                        // const obj1 = svgAllInfo[index].store.json.labels
                        // const len1 = obj1.length
                        // for (let index2 = 0; index2 < len1; index2++) {
                        //     if (obj1[index2].id === id) {
                        //         params.push({
                        //             attrMappingId: item.attrMappingId,
                        //             rowIndex: item.rowIndex,
                        //             labelId: obj1[index2].categoryId,
                        //             fromId: obj1[index2].startIndex,
                        //             toId: obj1[index2].endIndex,
                        //             evalId: item.evalId
                        //         })
                        //         break;
                        //     }
                        // }
                        let flag = false
                        svgAllInfo[index].store.json.connections.map(item2 => {
                            if (item2.fromId === id || item2.toId === id) {
                                flag = true
                            }
                        })
                        if (flag) {
                            message.error('当前实体含有关系，请先删除相关关系！')
                        } else {
                            annotator.applyAction(Action.Label.Delete(id))
                            api.saveSvgInfo({
                                attrMappingId: item.attrMappingId,
                                rowIndex: item.rowIndex,
                                evalId: item.evalId,
                                annotation: annotator.store.json
                            }, taskId).then(res => {
                                // getSvgList()
                                setVisible(false)
                                setRadioValue(null)
                            }).catch(err => { })
                        }
                        // api.saveSvgInfo({
                        //     attrMappingId: item.attrMappingId,
                        //     rowIndex: item.rowIndex,
                        //     evalId: item.evalId,
                        //     annotation: annotator.store.json
                        // }, taskId).then(res => {
                        //     getSvgList()
                        //     setVisible(false)
                        //     setRadioValue(null)
                        // }).catch(err => { })
                        // api.delSvgInfo(params).then(res => {
                        //     svgAllInfo[index].applyAction(Action.Label.Delete(id))
                        //     api.saveSvgInfo({
                        //         attrMappingId: item.attrMappingId,
                        //         rowIndex: item.rowIndex,
                        //         evalId: item.evalId,
                        //         annotation: svgAllInfo[index].store.json
                        //     }, taskId).then(res => {
                        //         getMainSvgList()
                        //     }).catch(err => { })
                        // }).catch(err => { })
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

    const radioChange = ({ target: { value } }) => {
        setRadioValue(value)
    }



    // 输入框事件
    function inputChange({ target: { value } }, type) {
        if (type === 'drawer') {
            setKeyWord2(value)
        } else {
            setKeyWord(value)
        }
    }

    // 翻页
    function pageChange(page, pageSize, type) {
        if (type === 'drawer') {
            $('.svgContainer').remove()
            setDrawerPageCurrent(page)
            setTabSpin(true)
            setSchemaList([])
        } else {
            setSvgSpinning(true)
            $('.svgMainContainer').remove()
            setSvgList([])
            setPageCurrent(page)
            setPageSize(pageSize)
        }
    }


    // 选择框事件
    function selChange(value, type) {
        if (type === 'isMark') {
            setSpinning(true)
            setIsMark(value)
        } else if (type === 'isMark2') {
            setIsMark2(value)
            setSvgSpinning(true)
            $('.svgMainContainer').remove()
            setSvgList([])
        } else if (type === 'typeCode') {
            $('.svgContainer').remove()
            setDrawerPageCurrent(1)
            setTabSpin(true)
            setSpinning2(true)
            setDrawerLiList([])
            setSchemaList([])
            setTypeCode(value)
        }
    }

    // 左侧列表点击事件
    function liClick(item, index, type) {
        if (type === 'level1') {
            setLiLevel('level2')
            setCurrentSelectIndex(index)
            setIsMark('all')
            setKeyWord(null)
            setTableOrigin(item.tableOrigin)
            setTableMappingId(item.tableMappingId)
        } else if (type === 'level2') {
            setLiLevel('level1')
            setCurrentSelectFieldIndex(null)
            setAttrMappingId(null)
            setIsMark('all')
            setKeyWord(null)
        } else if (type === 'field') {
            if (currentSelectFieldIndex !== index) {
                const { attrMappingId, attrOrigin } = item
                setSvgSpinning(true)
                setCurrentSelectFieldIndex(index)
                setAttrMappingId(attrMappingId)
                setColumnName(attrOrigin)
                $('.svgMainContainer').remove()
                setSvgList([])
                setPageCurrent(1)
            }
        } else if (type === 'drawer') {
            if (drawerSelectIndex !== index) {
                const { definition, schemaId } = item
                $('.svgContainer').remove();
                setDrawerSelectIndex(index)
                setSchemaId(schemaId)
                setTag('01')
                setTabPaneKey('1')
                setSchemaList([])
                setDefinition(definition)
                setTabSpin(true)
            }
        }
    }

    // 按钮事件
    function btnClick(type) {
        if (type === 'checkGuideLine') {
            setDrawerVisible(true)
        } else if (type === 'finish') {
            api.getSubmitInfo({
                taskId
            }).then(res => {
                const { data: { publishComfirm, publishState } } = res
                setPublishState(publishState)
                setModalContent(publishComfirm)
                setMyModalVisible(true)
            }).catch(err => { })
        } else if (type === 'cancel') {
            window.close()
        }
    }

    function modalOk(type) {
        if (type === 'confirm') {
            api.confirmSubmit({
                taskId
            }).then(res => {
                message.success('提交成功,窗口即将关闭！')
                window.opener.location.reload();
                setTimeout(() => {
                    window.close()
                }, 500);
            }).catch(err => { })
        } else if (type === '1') {
            if (radioValue !== '' && radioValue !== undefined && radioValue !== null) {
                svgAllInfo[currentSvg].applyAction(Action.Label.Create(radioValue, startIndex, endIndex))
                api.saveSvgInfo({
                    attrMappingId,
                    rowIndex,
                    evalId,
                    annotation: svgAllInfo[currentSvg].store.json
                }, taskId).then(res => {
                    getSvgList()
                    setVisible(false)
                    setRadioValue(null)
                }).catch(err => { })
            } else {
                message.error('请选择标签！')
            }
        } else if (type === '2') {
            if (radioValue !== '' && radioValue !== undefined && radioValue !== null) {
                svgAllInfo[currentSvg].applyAction(Action.Connection.Create(radioValue, first, second))
                api.saveSvgInfo({
                    attrMappingId,
                    rowIndex,
                    evalId,
                    annotation: svgAllInfo[currentSvg].store.json
                }, taskId).then(res => {
                    setVisible2(false)
                    setRadioValue(null)
                    getSvgList()
                }).catch(err => { })
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
                    <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Select value={isMark} onSelect={(value) => selChange(value, 'isMark')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
                                    <Option value='all'>全部</Option>
                                    <Option value='01'>已标注</Option>
                                    <Option value='02'>未标注</Option>
                                </Select>
                                <Input.Search value={keyWord} placeholder='请输入表名' onChange={(e) => inputChange(e, 'table')} onSearch={() => {
                                    if (liLevel === 'level1') {
                                        getTableList()
                                    } else {
                                        getFieldList()
                                    }
                                }} enterButton />
                            </div>
                            <ul className={'mds-executePage-ulWrap'} style={liLevel === 'level1' ? {
                                transform: 'translateX(0%)'
                            } : { transform: 'translateX(-101%)' }}>
                                {
                                    liList.length > 0
                                        ?
                                        liList.map((item, index) => {
                                            return (
                                                <Tooltip
                                                    key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                    getPopupContainer={() => document.getElementById('tooltipWrap')}
                                                    overlayClassName='liToolTip'
                                                    placement='right'
                                                    title={
                                                        <div dangerouslySetInnerHTML={{ __html: item.levitationTips }}>
                                                        </div>
                                                    }
                                                >
                                                    <li
                                                        onClick={() => liClick(item, index, 'level1')}
                                                        className={
                                                            currentSelectIndex === index
                                                                ?
                                                                'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                :
                                                                (
                                                                    item.isMark === 'no'
                                                                        ?
                                                                        'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                        :
                                                                        'mds-executePage-liWrap mds-executePage-liSelect'
                                                                )

                                                        }
                                                    >
                                                        <span className='mds-executePage-liName'>{item.tableName}</span>
                                                    </li>
                                                </Tooltip>
                                            )
                                        })
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
                            </ul>
                            <div className={'mds-executePage-ulWrap'} style={liLevel === 'level2' ? {
                                transform: 'translateX(0%)'
                            } : { transform: 'translateX(-101%)' }}>
                                <ul>
                                    {
                                        fieldList.length > 0
                                            ?
                                            fieldList.map((item, index) => {
                                                return (
                                                    <li
                                                        key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                        onClick={() => liClick(item, index, 'field')}
                                                        className={
                                                            currentSelectFieldIndex === index
                                                                ?
                                                                'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                :
                                                                (
                                                                    item.isMark === 'no'
                                                                        ?
                                                                        'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                        :
                                                                        'mds-executePage-liWrap mds-executePage-liSelect'
                                                                )

                                                        }
                                                    >
                                                        <span className='mds-executePage-liName'>{item.attrName}</span>
                                                    </li>
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </ul>
                                {React.createElement(MenuFoldOutlined, {
                                    className: 'mds-formalMark-trigger',
                                    style: {
                                        display: liLevel === 'level1' ? 'none' : 'block',
                                    },
                                    onClick: () => liClick('', '', 'level2'),
                                })}
                            </div>

                        </div>
                    </Spin>
                    <div className='mds-formalMark-rightWrap' >
                        <div className='mds-formalMark-rightHead'>
                            <Select value={isMark2} onChange={(value) => selChange(value, 'isMark2')} style={{ width: '200px' }}>
                                <Option value='all'>全部</Option>
                                <Option value='01'>已标注</Option>
                                <Option value='02'>未标注</Option>
                            </Select>
                            <MyButton label='查看GUIDELINE' onClick={() => btnClick('checkGuideLine')} />
                        </div>
                        <div className='mds-formalMark-rightContent'>
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
                            className='mds-formalMark-svgPageNation'
                            current={pageCurrent}
                            showQuickJumper
                            pageSize={pageSize}
                            onChange={pageChange}
                            total={pageCount}
                        />
                        <div className='mds-formalMark-btns'>
                            <Space size='large'>
                                <MyButton label='完成' onClick={() => btnClick('finish')} />
                                <MyButton label='取消' classType='warm' onClick={() => btnClick('cancel')} />
                            </Space>
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
                    <Spin spinning={spinning2} tip='加载中...' style={{ zIndex: '1' }}>
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
                                <Input onChange={(e) => inputChange(e, 'drawer')} onPressEnter={() => {
                                    $('.svgContainer').remove();
                                    setSchemaId(null)
                                    setDrawerPageCurrent(1)
                                    setDrawerLiList([])
                                    setSchemaList([])
                                    getLiList()
                                }} placeholder='请输入标签名' />
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                {
                                    drawerLiList.length > 0
                                        ?
                                        drawerLiList.map((item, index) => {
                                            return (
                                                <li
                                                    key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                    onClick={() => liClick(item, index, 'drawer')}
                                                    className={
                                                        drawerSelectIndex === index
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
                                {
                                    definition
                                        ?
                                        <Tooltip title={definition}>
                                            {definition}
                                        </Tooltip>
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
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
                                                current={drawerPageCurrent}
                                                pageSize={drawerPageSize}
                                                onChange={(page, pageSize) => pageChange(page, pageSize, 'drawer')}
                                                total={drawerPageCount}

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
                                                current={drawerPageCurrent}
                                                pageSize={drawerPageSize}
                                                onChange={(page, pageSize) => pageChange(page, pageSize, 'drawer')}
                                                total={drawerPageCount}
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
                title={'提示'}
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
