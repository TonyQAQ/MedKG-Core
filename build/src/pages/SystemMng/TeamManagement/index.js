import React, { Fragment, useState, useEffect } from 'react'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import { Button, Spin, Form, Input, Modal, Space, message } from 'antd'
import MyTable from '@/components/Table'
import MyModal from '@/components/Modal'
import * as api from '../services'
import '../index.less'

export default function Index() {
    const [spinning, setSpinning] = useState(false)
    const [modalVisible, setModalVisible] = useState(false)
    const [spinTip, setsSpinTip] = useState('')
    const [dataSource, setDataSource] = useState([])
    const [columns, setColumns] = useState([])
    const [tableLoading, setTableLoading] = useState(true)
    const [pageCount, setPageCount] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageTotal, setPageTotal] = useState(0)
    const [visible, setVisible] = useState(false);
    const [keyWord, setKeyWord] = useState('')
    const [organizationId, setOrganizationId] = useState('')
    const [orgCode, setOrgCode] = useState('')
    const [currentRecord, setCurrentRecord] = useState(null)
    const [modalTitle, setModalTitle] = useState('')
    const [modalContent, setModalContent] = useState('')
    const [actionType, setActionType] = useState('')

    useEffect(() => {
        getList()
    }, [])

    const getList = () => {
        api.getList({ keyWord }).then(res => {
            const { data, titleHead } = res
            setDataSource(data)
            let newColumns = [...titleHead]
            newColumns.push({
                title: "操作",
                key: 'action',
                fixed: 'right',
                width: 270,
                render: (text, record) => (
                    <Space size="middle">
                        {/* <MyButton label='加入团队' onClick={() => tableActionClick(record, 'joinTeam')} /> */}
                        <MyButton label='编辑' classType='orange' onClick={() => tableActionClick(record, 'edit')} />
                        <MyButton label='删除' danger onClick={() => tableActionClick(record, 'delete')} />
                    </Space>
                ),
            })
            setColumns(newColumns)
            setTableLoading(false)
        }).catch(err => { })
    }

    const onCreate = (values) => {
        console.log('Received values of form: ', values);
        if (actionType === 'new') {
            const { orgName } = values
            api.saveOrUpdate({
                organizationId,
                orgCode,
                orgName
            }).then(res => {
                setVisible(false)
                setActionType('')
                setTableLoading(true)
                getList()
            }).catch(err => {
                setVisible(false)
            })
        } else {
            const { orgName } = values
            api.saveOrUpdate({
                organizationId: currentRecord.code,
                orgCode: currentRecord.code,
                orgName
            }).then(res => {
                setVisible(false)
                setActionType('')
                setTableLoading(true)
                getList()
            }).catch(err => {
                setVisible(false)
            })
        }
    };

    function tableActionClick(record, type) {
        const { } = record
        switch (type) {
            case 'joinTeam': {
                setActionType('joinTeam')
                setModalVisible(true)
                setModalTitle('操作')
                setModalContent('确认加入该团队吗？')
                setCurrentRecord(record)
                break;
            }
            case 'edit': {
                setCurrentRecord(record)
                setActionType('edit')
                setVisible(true)
                break;
            }
            case 'delete': {
                setActionType('delete')
                setModalVisible(true)
                setModalTitle('删除')
                setModalContent('确认删除该团队吗？')
                setCurrentRecord(record)
                break;
            }
        }
    }

    // 翻页
    const pageChange = () => {

    }

    // 删除确认
    const confirmClick = () => {
        if (actionType === 'delete') {
            api.delTeamInfo({
                organizationId: currentRecord.code
            }).then(res => {
                message.success('删除成功！')
                setModalVisible(false)
                setTableLoading(true)
                getList()
            }).catch(err => { })
        } else {

        }
    }

    // 输入框事件
    function inputChange({ target: { value } }, type) {
        if (type === 'keyWord') {
            setKeyWord(value)
        }
    }

    return (
        <Fragment>
            <div className="mds-actionsWrap" style={{ justifyContent: '' }}>
                <Button type='primary' size='large' onClick={() => {
                    setActionType('new')
                    setVisible(true)
                }}>新增团队</Button>
                <div className='mds-conditionWrap'>
                    <InputWrap label='名称' size='large' inputChange={(e) => inputChange(e, 'keyWord')} />
                    <MyButton
                        label='搜索'
                        size='large'
                        onClick={() => {
                            setTableLoading(true)
                            getList()
                        }}
                    />
                </div>
            </div>
            <Spin spinning={spinning} tip={spinTip}>
                <MyTable
                    dataSource={dataSource}
                    columns={columns}
                    loading={tableLoading}
                    pageCount={pageCount}
                    pagination={false}
                    pageSize={pageSize}
                    pageChange={pageChange}
                    total={pageTotal}
                />
            </Spin>
            <CollectionCreateForm
                visible={visible}
                onCreate={onCreate}
                actionType={actionType}
                currentRecord={currentRecord}
                onCancel={() => {
                    setVisible(false);
                }}
            />
            <MyModal
                visible={modalVisible}
                modalCancel={() => {
                    setModalVisible(false)
                }}
                title={modalTitle}
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={confirmClick}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={() => {
                            setModalVisible(false)
                        }} />
                    </Fragment>
                }
            >
                <Fragment>
                    <p>{modalContent}</p>
                </Fragment>
            </MyModal>
        </Fragment>
    )
}


const CollectionCreateForm = ({ visible, onCreate, onCancel, actionType, currentRecord }) => {
    const [form] = Form.useForm();
    const formItemLayout = {
        labelCol: {
            xs: { span: 24 },
            sm: { span: 4 },
        },
        wrapperCol: {
            xs: { span: 24 },
            sm: { span: 20 },
        },
    };
    form.setFieldsValue({
        orgName: actionType === 'edit' ? currentRecord && currentRecord.value : null
    })
    return (
        <Modal
            visible={visible}
            maskClosable={false}
            title={actionType === 'new' ? "新增团队" : "编辑团队"}
            destroyOnClose={true}
            footer={
                <Fragment>
                    <Button type={'primary'} onClick={() => {
                        form
                            .validateFields()
                            .then((values) => {
                                form.resetFields();
                                onCreate(values);
                            })
                            .catch((info) => {
                                console.log('Validate Failed:', info);
                            });
                    }}>确定</Button>
                    <MyButton label='取消' classType='warm' onClick={() => {
                        form.resetFields()
                        onCancel()
                    }} />
                </Fragment>
            }
            onCancel={() => {
                form.resetFields()
                onCancel()
            }}
        >
            <Form
                form={form}
                name="form_in_modal"
                {...formItemLayout}
            >
                <Form.Item name="orgName" label="团队名称"
                    rules={[
                        {
                            required: true,
                            message: '请输入团队名称!',
                        },
                    ]}
                >
                    <Input />
                </Form.Item>
            </Form>
        </Modal>
    );
};