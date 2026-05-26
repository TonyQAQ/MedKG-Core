import React, { Fragment, useState, useEffect } from 'react'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import { Button, Spin, Form, Input, Modal, Select, message, Space } from 'antd'
import MyTable from '@/components/Table'
import MyModal from '@/components/Modal'
import * as api from '../services'
import '../index.less'

const { Option } = Select
const { encryptedString, RSAKeyPair } = window

export default function Index() {
    const [teamSelList, setTeamSelList] = useState([])
    const [rolesList, setRolesList] = useState([])
    const [spinning, setSpinning] = useState(false)
    const [spinTip, setsSpinTip] = useState('')
    const [dataSource, setDataSource] = useState([])
    const [columns, setColumns] = useState([])
    const [tableLoading, setTableLoading] = useState(true)
    const [pageCount, setPageCount] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageTotal, setPageTotal] = useState(0)
    const [visible, setVisible] = useState(false);
    const [organizationId, setOrganizationId] = useState(null)
    const [userId, setUserId] = useState(null)
    const [keyWord, setKeyWord] = useState(null)
    const [modalTitle, setModalTitle] = useState('')
    const [modalContent, setModalContent] = useState('')
    const [actionType, setActionType] = useState('')
    const [modalVisible, setModalVisible] = useState(false)
    const [currentRecord, setCurrentRecord] = useState(null)

    useEffect(() => {
        getList()
        getRolesInfo()
        getRoleTable()
    }, [])

    useEffect(() => {
        if (organizationId && pageCount) {
            getRoleTable()
        }
    }, [organizationId, pageCount])


    // 查询人员表格列表
    const getRoleTable = () => {
        api.getRoleTable({
            organizationId,
            userId,
            pageSize,
            pageCount,
            keyWord
        }).then(res => {
            const { data, titleHead, totalCount } = res
            let newColumns = [...titleHead]
            newColumns.push({
                title: "操作",
                key: 'action',
                fixed: 'right',
                width: 270,
                render: (text, record) => (
                    <Space size="middle">
                        <MyButton label='重置密码' onClick={() => tableActionClick(record, 'reset')} />
                        <MyButton label='编辑' classType='orange' onClick={() => tableActionClick(record, 'edit')} />
                        <MyButton label='删除' disabled={record.bingCount > 0 || record.roleTag === 'yes' ? true : false} danger onClick={() => tableActionClick(record, 'delete')} />
                    </Space>
                ),
            })
            setTableLoading(false)
            setDataSource(data)
            setPageTotal(totalCount)
            setColumns(newColumns)
        }).catch(err => {
            setTableLoading(false)
        })
    }

    // 查询团队列表
    const getList = () => {
        api.getList().then(res => {
            const { data, titleHead } = res
            setTeamSelList(data)
        }).catch(err => { })
    }

    // 查询人员下拉列表
    const getRolesInfo = () => {
        api.getRolesInfo().then(res => {
            const { data } = res
            setRolesList(data)
        }).catch(err => { })
    }


    // 新增弹窗确认按钮
    const onCreate = (values) => {
        if (actionType === 'new') {
            const { userName, account, organizationId, roleId, password } = values
            api.newAddRole({
                userName,
                account,
                organizationId,
                roleId,
                password: password ? encryptedString(new RSAKeyPair("10001", "", "b582bfab21f625687e985e19d"), password) : ''
            }).then(res => {
                message.success('新增成功！')
                setVisible(false);
                getRoleTable()
                setTimeout(() => {
                    setActionType('')
                }, 500);
            }).catch(err => { })
        } else {
            const { userName, account, organizationId, roleId } = values
            const { userId } = currentRecord
            api.editRoleInfo({
                userId,
                account,
                username: userName,
                organizationId: currentRecord.organizationId,
                newOrganizationId: organizationId !== currentRecord.organizationId ? organizationId : '',
                roleId: currentRecord.roleId,
                newRoleId: roleId !== currentRecord.roleId ? roleId : ''
            }).then(res => {
                message.success('修改成功！')
                getRoleTable()
                setVisible(false)
                setTimeout(() => {
                    setActionType('')
                }, 500);
            }).catch(err => {
                setVisible(false)
            })
        }


    };

    // 表格按钮事件
    function tableActionClick(record, type) {
        setActionType(type)
        switch (type) {
            case 'edit': {
                api.getRoleTable({
                    userId: record.userId,
                    pageSize,
                    pageCount,
                }).then(res => {
                    setCurrentRecord(res.data[0])
                    setVisible(true)
                }).catch(err => { })
                break;
            }
            case 'delete':
            case 'reset': {
                setCurrentRecord(record)
                setModalVisible(true)
                setModalTitle(type === 'delete' ? '删除' : '重置')
                setModalContent(type === 'delete' ? '确认删除该人员吗？' : '确认重置密码为默认密码吗？')
                break;
            }
        }
    }

    // 翻页
    const pageChange = (page, pageSize) => {
        setPageCount(page)
    }


    // 选择框事件
    const selChange = (value) => {
        setTableLoading(true)
        setOrganizationId(value)
    }


    // 输入框事件
    const inputChange = ({ target: { value } }) => {
        setKeyWord(value)
    }

    // 删除确认
    const confirmClick = () => {
        if (actionType === 'delete') {
            api.delRoleInfo({
                userId: currentRecord.userId
            }).then(res => {
                message.success('删除成功！')
                setModalVisible(false)
                setTableLoading(true)
                getRoleTable()
            }).catch(err => { })
        } else {
            api.editRoleInfo({
                userId: currentRecord.userId,
                reset: 'yes'
            }).then(res => {
                message.success('重置成功！')
                setModalVisible(false)
                getRoleTable()
            }).catch(err => { })
        }
    }

    return (
        <Fragment>
            <div className="mds-actionsWrap">
                <Button type='primary' size='large' onClick={() => {
                    setActionType('new')
                    setVisible(true)
                }}>新增人员</Button>
                <div className='mds-conditionWrap'>
                    <SelectWrap
                        style={{ width: '200px' }}
                        label='团队' size='large'
                        list={teamSelList}
                        selChange={selChange}
                    />
                    <InputWrap label='名称' size='large' inputChange={inputChange} />
                    <MyButton
                        label='搜索'
                        size='large'
                        onClick={() => {
                            setTableLoading(true)
                            getRoleTable()
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
                    pagination={true}
                    pageSize={pageSize}
                    pageChange={pageChange}
                    total={pageTotal}
                />
            </Spin>
            <CollectionCreateForm
                visible={visible}
                onCreate={onCreate}
                teamSelList={teamSelList}
                rolesList={rolesList}
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


const CollectionCreateForm = ({ visible, onCreate, onCancel, teamSelList, rolesList, actionType, currentRecord }) => {
    const [form] = Form.useForm();
    const formItemLayout = {
        labelCol: {
            xs: { span: 24 },
            sm: { span: 3 },
        },
        wrapperCol: {
            xs: { span: 24 },
            sm: { span: 21 },
        },
    };
    form.setFieldsValue({
        userName: actionType === 'edit' ? currentRecord && currentRecord.username : '',
        account: actionType === 'edit' ? currentRecord && currentRecord.account : '',
        organizationId: actionType === 'edit' ? currentRecord && currentRecord.organizationId : '',
        roleId: actionType === 'edit' ? currentRecord && currentRecord.roleId : ''
    })
    return (
        <Modal
            visible={visible}
            title="基本信息"
            maskClosable={false}
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
                <Form.Item name="userName" label="姓名"
                    rules={[
                        {
                            required: true,
                            message: '请输入姓名!',
                        },
                    ]}
                >
                    <Input />
                </Form.Item>
                <Form.Item name="account" label="帐号"
                    rules={[
                        {
                            required: true,
                            message: '请输入帐号!',
                        },
                    ]}
                >
                    <Input />
                </Form.Item>
                <Form.Item name="organizationId" label="团队"
                    rules={[
                        {
                            required: true,
                            message: '请选择团队!',
                        },
                    ]}
                >
                    <Select disabled={currentRecord && currentRecord.bingCount > 0 ? true : false}>
                        {
                            teamSelList.map(item => {
                                return (
                                    <Option key={item.code} value={item.code}>{item.value}</Option>
                                )
                            })
                        }
                    </Select>
                </Form.Item>
                <Form.Item name="roleId" label="角色"
                    rules={[
                        {
                            required: true,
                            message: '请选择角色!',
                        },
                    ]}
                >
                    <Select>
                        {
                            rolesList.map(item => {
                                return (
                                    <Option key={item.code} value={item.code}>{item.value}</Option>
                                )
                            })
                        }
                    </Select>
                </Form.Item>
                {
                    actionType === 'edit'
                        ?
                        null
                        :
                        <Form.Item name="password" label="密码">
                            <Input.Password />
                        </Form.Item>
                }
            </Form>
        </Modal>
    );
};