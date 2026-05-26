import React, { Fragment, useEffect, useState } from 'react';
import LayoutHeader from '@/components/Header';
import { Select, Spin, Tooltip, Empty, Input, Modal, Button, message, Tree, Pagination, Radio, Row } from 'antd';
import { MyButton } from '@/components/commonWrap';
import { PlusSquareOutlined, FormOutlined, MinusCircleOutlined, TagOutlined } from '@ant-design/icons';
import * as api from './services';
import MyModal from '@/components/Modal'
import $ from 'jquery'
import './index.less';
import InfiniteScroll from 'react-infinite-scroller';

export default function Index(props) {
    const [taskId, setTaskId] = useState(null)
    const [spinning, setSpinning] = useState(true)
    const [spinning2, setSpinning2] = useState(true)
    const [svgSpinning, setSvgSpinning] = useState(true)
    const [labelAddVisible, setLabelAddVisible] = useState(false)
    const [keyWord, setKeyWord] = useState('')
    const [svgList, setSvgList] = useState([])
    const [labelTypeList, setLabelTypeList] = useState([])
    const [currentSelectLabelIndex, setcurrentSelectLabelIndex] = useState(null)
    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageCount, setPageCount] = useState(0)
    const [newAddLabelVal, setNewAddLabelVal] = useState(null)
    const [treeData, setTreeData] = useState([])
    const [treeDataTemp, setTreeDataTemp] = useState([])
    const [paths, setPaths] = useState('')
    const [isLeaf, setIsLeaf] = useState(true)
    const [canGetSvg, setCanGetSvg] = useState(false)
    const [focusIndex, setFocusIndex] = useState(null)
    const [labelModalIsEdit, setLabelModalIsEdit] = useState(false)
    const [labelIndex, setLabelIndex] = useState(null)
    const [expandedKeys, setExpandedKeys] = useState([])
    const [autoExpandParent, setAutoExpandParent] = useState(true)
    const [treeDataList, setTreeDataList] = useState([])
    const [myModalVisible, setMyModalVisible] = useState(false)
    const [selectedKeys, setSelectedKeys] = useState([])
    const [fromTaskId, setFromTaskId] = useState('')
    const [hasMore, setHasMore] = useState(true)
    const [sliderLoading, setSliderLoading] = useState(true)
    const [allContent, setAllContent] = useState([])

    useEffect(() => {
        const { taskId, fromTaskId } = props.match.params
        setTaskId(taskId)
        setFromTaskId(fromTaskId)
    }, [])

    useEffect(() => {
        if (taskId && fromTaskId) {
            getTreeData()
            getClassifyList()
        }
    }, [taskId, fromTaskId])

    useEffect(() => {
        if (paths && isLeaf && pageCurrent) {
            getMarkDatasList()
        }
    }, [paths, isLeaf, pageCurrent])

    useEffect(() => {
        if (canGetSvg) {
            getMarkDatasList()
        }
    }, [canGetSvg])


    const getTreeData = () => {
        api.getTreeData({
            taskId
        }).then(res => {
            const { tree } = res
            const newTree = repalceTree(tree)
            let dataList = [], paths, openKeys = [], selectedKeys = []
            const generateList = data => {
                for (let i = 0; i < data.length; i++) {
                    const node = data[i];
                    const { key, title } = node;
                    dataList.push({ key, title });
                    if (node.children) {
                        generateList(node.children);
                    }
                }
            };
            const getFirstPaths = (data) => {
                for (let index = 0; index < data.length; index++) {
                    const item = data[index];
                    if (item.children && item.children.length > 0) {
                        openKeys.push(item.key)
                        return getFirstPaths(item.children)
                    } else {
                        paths = item.path
                        selectedKeys.push(item.key)
                        break;
                    }
                }
            }
            getFirstPaths(newTree)
            generateList(newTree)
            setPaths(paths)
            setSelectedKeys(selectedKeys)
            setTreeDataList(dataList)
            setExpandedKeys(openKeys)
            setTreeData(newTree)
            setTreeDataTemp(newTree)
            setSpinning(false)
        }).catch(err => {
            setSpinning(false)
        })
    }

    const repalceTree = (tree, node) => {
        return tree.map(item => {
            if (item.children && item.children.length > 0) {
                item.path = (node && node.path ? node.path + ',' : '') + item.title
                repalceTree(item.children, item)
                return item
            } else {
                item.isLeaf = true
                item.path = (node && node.path ? node.path + ',' : '') + item.title
                return item
            }
        })
    }

    // 获取标签分类列表
    const getClassifyList = () => {
        api.getClassifyList({ taskId }).then(res => {
            const { classifyCategories } = res
            setLabelTypeList(classifyCategories)
            setSpinning2(false)
        }).catch(err => { })
    }

    // 获取svg列表
    const getMarkDatasList = () => {
        api.getMarkDatasList({
            taskId,
            pageSize: fromTaskId === 'true' ? pageSize : 1,
            pageCount: pageCurrent,
            paths,
            peddleType: fromTaskId === 'true' ? 'normal' : 'sliding'
        }).then(res => {
            setSvgList(res.data)

            setPageCount(res.totalCount)
            setSvgSpinning(false)
            setCanGetSvg(false)
            if (fromTaskId === 'false') {
                setTimeout(() => {
                    setSliderLoading(false)
                    setAllContent(res.data.length > 0 ? allContent.concat(res.data[0].annotation.content) : [])
                }, 200);
            }

        }).catch(err => {

        })
    }
    // 翻页
    const pageChange = (page, pageSize) => {
        setSvgSpinning(true)
        setSvgList([])
        setPageCurrent(page)
    }
    // 弹窗取消
    const modalCancel = () => {
        setLabelAddVisible(false)
        setLabelModalIsEdit(false)
        setNewAddLabelVal(null)
    }
    // 树节点选择
    const onSelect = (selectedKeys, e) => {
        if (e.node.path !== paths) {
            if (e.node.isLeaf) {
                setSvgSpinning(true)
                setSvgList([])
                setAllContent([])
                setHasMore(true)
                setSliderLoading(true)
                setPageCurrent(1)
                setPaths(e.node.path)
                setIsLeaf(e.node.isLeaf)
            }
            setSelectedKeys(selectedKeys)
        }
    }

    const onExpand = expandedKeys => {
        setExpandedKeys(expandedKeys)
        setAutoExpandParent(false)
    };
    const getParentKey = (key, tree) => {
        let parentKey;
        for (let i = 0; i < tree.length; i++) {
            const node = tree[i];
            if (node.children) {
                if (node.children.some(item => item.key === key)) {
                    parentKey = node.key;
                } else if (getParentKey(key, node.children)) {
                    parentKey = getParentKey(key, node.children);
                }
            }
        }
        return parentKey;
    };

    function inputSearch(value, type) {
        if (type === 'keyWord') {
            setTreeData([])
            const expandedKeys = treeDataList
                .map(item => {
                    if (item.title.indexOf(value) > -1) {
                        return getParentKey(item.key, treeData);
                    }
                    return null;
                })
                .filter((item, i, self) => item && self.indexOf(item) === i);
            const result = loop(treeDataTemp)
            setTreeData(result)
            setExpandedKeys(expandedKeys)
            setAutoExpandParent(true)
        }
    }
    // 左侧列表输入框搜索事件
    function inputChange({ target: { value } }, type) {
        if (type === 'modalLabel') {
            setNewAddLabelVal(value)
        } else {
            setKeyWord(value)
        }
    }
    // 左侧列表点击事件
    function liClick(item, index) {

    }
    // 按钮事件
    function btnClick(type) {
        if (type === 'newAddLabel') {
            setLabelAddVisible(true)
        } else if (type === 'cancel') {
            window.close()
        } else if (type === 'finish') {
            setMyModalVisible(true)
        } else if (type === 'confirmSubmit') {
            api.updateTaskStatu({
                taskId,
                stateCode: '400000'
            }).then(res => {
                message.loading('任务实例生成中...', 0)
                api.createInstance({
                    taskId
                }).then(res => {
                    message.destroy()
                    message.loading('任务实例生成成功！')
                    window.opener.location.reload();
                    setMyModalVisible(false)
                    setTimeout(() => {
                        window.close()
                    }, 500);
                })
            })
        } else if (type === 'save') {
            message.success('保存成功！')
        }
    }
    const updateLabelAfterEdit = () => {
        let newResult
        newResult = JSON.parse(JSON.stringify(labelTypeList))
        newResult[labelIndex].category = newAddLabelVal
        api.updateLabel(newResult, taskId).then(res => {
            setLabelAddVisible(false)
            setLabelModalIsEdit(false)
            setNewAddLabelVal(null)
            getClassifyList()
        }).catch(err => {

        })
    }
    // 弹窗确认
    function modalOk(type) {
        if (type === 'confirmNewAddLabel') {
            if (labelModalIsEdit) {
                if (newAddLabelVal) {
                    updateLabelAfterEdit()
                } else {
                    message.error('不能为空！')
                }
            } else {
                if (!newAddLabelVal) {
                    message.error('不能为空！')
                } else {
                    let newLabel = {
                        id: labelTypeList.length,
                        category: newAddLabelVal
                    }
                    let newLabels = JSON.parse(JSON.stringify(labelTypeList))
                    newLabels.push(newLabel)
                    api.updateLabel(newLabels, taskId).then(res => {
                        message.success('添加成功！')
                        setLabelAddVisible(false)
                        setNewAddLabelVal(null)
                        getClassifyList()
                    }).catch(err => {

                    })
                }
            }
        }
    }


    const svgRadioChange = (item, e) => {
        setSvgSpinning(true)
        let newObj = JSON.parse(JSON.stringify(item))
        newObj.categories = []
        newObj.categories.push(e.target.value)
        api.saveSvgInfo([newObj], taskId, paths).then(res => {
            setSvgSpinning(false)
        }).catch(err => {
            setCanGetSvg(true)
            setSvgSpinning(false)
        })
    }
    // 右侧目录li鼠标移入移出事件
    function liMouseEvent(type, index) {
        if (type === 'enter') {
            setFocusIndex(index)
        } else if (type === 'leave') {
            setFocusIndex(null)
        }
    }
    // 右侧目录li的icon点击事件
    function iconClick(item, type, index) {
        if (type === 'edit') {
            setLabelAddVisible(true)
            setLabelModalIsEdit(true)
            setNewAddLabelVal(item.category)
            setLabelIndex(index)
        }
    }

    const loop = data =>
        data.map(item => {
            const index = item.title.indexOf(keyWord);
            const beforeStr = item.title.substr(0, index);
            const afterStr = item.title.substr(index + keyWord.length);
            const title =
                index > -1 ? (
                    <span>
                        {beforeStr}
                        <span className="site-tree-search-value">{keyWord}</span>
                        {afterStr}
                    </span>
                ) : (
                        <span>{item.title}</span>
                    );
            if (item.children.length > 0) {
                return { title, key: item.key, children: loop(item.children), path: item.path };
            } else {
                return {
                    title,
                    key: item.key,
                    path: item.path,
                    isLeaf: true
                };
            }
        });

    const handleInfiniteOnLoad = () => {
        if (svgList.length > 0) {
            let len = 0
            allContent.map(item =>
                len += item.length
            )
            console.log(len)
            if (len > svgList[0].endIndex) {
                message.warning('已经到底了！');
                setSliderLoading(false)
                setHasMore(false)
                return;
            }
            setSliderLoading(true)
            setTimeout(() => {
                setPageCurrent(pageCurrent + 1)
            }, 200);
        }

    };
    return (
        <div className='mds-all-layout-wrap' >
            <LayoutHeader />
            <div className='mds-executePage-wrap'>
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Input.Search placeholder='请输入表名' onChange={(e) => inputChange(e, 'keyWord')} onSearch={(value) => inputSearch(value, 'keyWord')} />
                            </div>
                            <div className='mds-marking-treeWrap'>
                                <Tree.DirectoryTree
                                    defaultExpandAll
                                    showIcon
                                    onExpand={onExpand}
                                    expandedKeys={expandedKeys}
                                    treeData={treeData}
                                    onSelect={onSelect}
                                    autoExpandParent={autoExpandParent}
                                    selectedKeys={selectedKeys}
                                />
                            </div>
                        </div>
                    </Spin>
                    <div className='mds-executePage-middleWrap' >
                        {
                            fromTaskId === 'true'
                                ?
                                <Fragment>
                                    <div className='mds-marking-middleWrap'>
                                        <Spin style={{ height: '100%' }} spinning={svgSpinning}>
                                            <div id='svgMainAllContainer'>
                                                {
                                                    svgList.length > 0
                                                        ?
                                                        svgList.map((item, index) => {
                                                            return (
                                                                <div className='svgMainContainer' id={'svgMainContainer' + index} >
                                                                    <div className='svgMainContainer-content'>{item.annotation.content}</div>
                                                                    <Row>
                                                                        <Radio.Group buttonStyle="solid" defaultValue={item.categories.toString()} onChange={(e) => svgRadioChange(item, e)}>
                                                                            {
                                                                                labelTypeList.map(radioItem => {
                                                                                    return (
                                                                                        <Tooltip title={radioItem.category}>
                                                                                            <Radio.Button value={radioItem.id} option={radioItem}><span><TagOutlined style={{ marginRight: '5px' }} />{radioItem.category} </span></Radio.Button>
                                                                                        </Tooltip>
                                                                                    )
                                                                                })
                                                                            }
                                                                        </Radio.Group>
                                                                    </Row>
                                                                </div>

                                                            )
                                                        })
                                                        :
                                                        <Empty style={{ marginTop: '35px' }} />
                                                }
                                            </div>
                                        </Spin>
                                    </div>

                                    <Pagination
                                        className='mds-marking-svgPageNation'
                                        current={pageCurrent}
                                        // showQuickJumper
                                        pageSize={pageSize}
                                        onChange={pageChange}
                                        total={pageCount}
                                    />
                                </Fragment>
                                :
                                <Spin style={{ height: '100%' }} spinning={svgSpinning}>
                                    <div id='svgMainAllContainer'>
                                        <InfiniteScroll
                                            initialLoad={false}
                                            pageStart={0}
                                            loadMore={handleInfiniteOnLoad}
                                            hasMore={!sliderLoading && hasMore}
                                            useWindow={false}
                                        >
                                            <div className='svgMainContainer-slider'>
                                                <Row align='middle'>
                                                    {
                                                        svgList.length > 0
                                                            ?
                                                            <Radio.Group buttonStyle="solid" defaultValue={svgList[0].categories.toString()} onChange={(e) => svgRadioChange(svgList[0], e)}>
                                                                {
                                                                    labelTypeList.map(radioItem => {
                                                                        return (
                                                                            <Tooltip title={radioItem.category}>
                                                                                <Radio.Button value={radioItem.id} option={radioItem}><span><TagOutlined style={{ marginRight: '5px' }} />{radioItem.category} </span></Radio.Button>
                                                                            </Tooltip>
                                                                        )
                                                                    })
                                                                }
                                                            </Radio.Group>
                                                            :
                                                            null
                                                    }

                                                </Row>
                                                <div style={{ whiteSpace: 'pre-line', }}>
                                                    {allContent}
                                                </div>
                                                {
                                                    sliderLoading
                                                        ?
                                                        <p style={{ textAlign: "center", marginTop: '30px' }}>
                                                            <Spin />
                                                        </p>
                                                        :
                                                        null
                                                }
                                            </div>

                                        </InfiniteScroll>
                                    </div>
                                </Spin>
                        }
                    </div>
                    <Spin spinning={spinning2} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-rightWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Button icon={<PlusSquareOutlined />} style={{ width: '100%' }} onClick={() => btnClick('newAddLabel')}>新增</Button>
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                {
                                    labelTypeList.length > 0
                                        ?
                                        labelTypeList.map((item, index) => {
                                            return (
                                                <li
                                                    onMouseEnter={() => liMouseEvent('enter', index)}
                                                    onMouseLeave={() => liMouseEvent('leave', index)}
                                                    key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                    onClick={() => liClick(item, index, 'right')}
                                                    className={
                                                        currentSelectLabelIndex === index
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
                                                    <span className='mds-executePage-liName'>{item.category}</span>
                                                    {
                                                        focusIndex === index
                                                            ?
                                                            <FormOutlined
                                                                className='liIcon editIcon'
                                                                onClick={() => iconClick(item, 'edit', index)}
                                                            />
                                                            :
                                                            null
                                                    }
                                                    {
                                                        focusIndex === index
                                                            ?
                                                            <MinusCircleOutlined
                                                                className='liIcon deleteIcon'
                                                                onClick={() => iconClick(item, 'del', index)}
                                                            />
                                                            :
                                                            null
                                                    }
                                                </li>
                                            )
                                        })
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
                            </ul>
                        </div>
                    </Spin>
                </div>
                <div className='mds-executePage-btns' >
                    <MyButton
                        label='保存'
                        onClick={() => btnClick('save')}
                        style={{ marginRight: '40px' }}
                    />
                    <MyButton
                        label='取消'
                        classType='warm'
                        onClick={() => btnClick('cancel')}
                        style={{ marginRight: '40px' }}
                    />
                </div>
            </div>
            <Modal
                title="新增类别"
                visible={labelAddVisible}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('confirmNewAddLabel')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <div className='mds-LabelDefinition-newAddModal'>
                    <Input.Group compact style={{ display: 'flex' }} >
                        <Input addonBefore="标签：" value={newAddLabelVal} onChange={(e) => inputChange(e, 'modalLabel')} />
                    </Input.Group>
                </div>
            </Modal>
            <MyModal
                visible={myModalVisible}
                modalCancel={() => {
                    setMyModalVisible(false)
                }}
                title={'提示'}
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => btnClick('confirmSubmit')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={() => {
                            setMyModalVisible(false)
                        }} />
                    </Fragment>

                }
            >
                <Fragment>
                    <p>提交后不可撤回，确认提交吗？</p>
                </Fragment>
            </MyModal>
        </div>
    )
}
