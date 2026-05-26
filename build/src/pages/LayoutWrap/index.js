import React, { Component, Fragment } from 'react'
import { Layout, Menu, Result } from 'antd';
import {
    MenuUnfoldOutlined,
    MenuFoldOutlined,
    HomeOutlined,
    DatabaseOutlined,
    CarryOutOutlined,
    SettingOutlined,
    DeploymentUnitOutlined,
    ShareAltOutlined,
    ApartmentOutlined,
    RobotOutlined
} from '@ant-design/icons';
import LayoutHeader from '@/components/Header'
import { Route, Link } from "react-router-dom";
import Ripples from 'react-touch-ripple';
import Icon from '@ant-design/icons';
import AsyncComponent from '@/components/asyncComponent'
import { connect } from 'react-redux'
import './index.less'

const { Sider, Content } = Layout;
const { SubMenu } = Menu;

const DataSourceMng = AsyncComponent(() => (import('../DataSourceMng')))
const HKKSIndex = AsyncComponent(() => (import('../HKKSIndex')))
const StructuredTask = AsyncComponent(() => (import('../TaskManagement')))
const TaskExecution = AsyncComponent(() => (import('../TaskExecution')))
const AtlasManagement = AsyncComponent(() => (import('../AtlasMng')))
const BasicDisasterRetrieval = AsyncComponent(() => (import('../KnowledgeApplication/BasicDisasterRetrieval')))
const DisasterAssociatedSearch = AsyncComponent(() => (import('../KnowledgeApplication/DisasterAssociatedSearch')))
const MembersManagement = AsyncComponent(() => (import('../SystemMng/MembersManagement')))
const TeamManagement = AsyncComponent(() => (import('../SystemMng/TeamManagement')))
const NLPTask = AsyncComponent(() => (import('../NaturalLanguage/NLPTask')))
const Demo = AsyncComponent(() => (import('../Demo')))

const Res = (param) => {
    return (
        <Result
            status="403"
            title="403"
            subTitle="抱歉，你没有权限访问当前页面。"
        />
    )
}

class LayoutWrap extends Component {
    constructor(props) {
        super(props)
        this.state = {
            collapsed: false,
            menuList: [
                //     {
                //     title: '首页',
                //     key: 'HKKSIndex',
                //     component: HKKSIndex
                // }, {
                //     title: '数据管理',
                //     key: 'DataSourceMng',
                //     component: DataSourceMng
                // }, {
                //     title: '任务管理',
                //     key: 'TaskManagement',
                //     children: [{
                //         title: '结构化任务',
                //         key: 'StructuredTask',
                //         component: StructuredTask,
                //         isChildren: true,
                //         typeCode: '01'
                //     }, {
                //         title: '非结构化任务',
                //         key: 'UnstructuredTask',
                //         component: StructuredTask,
                //         isChildren: true,
                //         typeCode: '02'
                //     }, {
                //         title: '标注任务',
                //         key: 'MarkingTasks',
                //         component: StructuredTask,
                //         isChildren: true,
                //         typeCode: '04'
                //     }]
                // }, {
                //     title: '任务执行',
                //     key: 'TaskExecution',
                //     children: [{
                //         title: '结构化任务',
                //         key: 'TaskExecutionStructured',
                //         component: TaskExecution,
                //         isChildren: true,
                //         typeCode: '01'
                //     }, {
                //         title: '非结构化任务',
                //         key: 'TaskExecutionUnStructured',
                //         component: TaskExecution,
                //         isChildren: true,
                //         typeCode: '02'
                //     }]
                // }, {
                //     title: '图谱应用',
                //     key: 'AtlasApplication',
                //     children: [{
                //         title: '图谱管理',
                //         key: 'AtlasManagement',
                //         component: AtlasManagement,
                //         isChildren: true
                //     },
                //     {
                //         title: '知识检索',
                //         key: 'KnowledgeRetrieval',
                //         component: BasicDisasterRetrieval,
                //         isChildren: true,
                //         type: '01'
                //     }, {
                //         title: '关联检索',
                //         key: 'RelatedSearch',
                //         component: DisasterAssociatedSearch,
                //         isChildren: true,
                //         type: '01'
                //     }]
                // },
                {
                    title: '自然语言',
                    key: 'NaturalLanguage',
                    children: [{
                        title: '句子切分',
                        key: 'SentenceSegmentationTask',
                        component: NLPTask,
                        isChildren: true,
                        typeCode: 'SST'
                    }, {
                        title: '中文分词',
                        key: 'WordSegmentationTask',
                        component: NLPTask,
                        isChildren: true,
                        typeCode: 'WST'
                    }, {
                        title: '实体抽取',
                        key: 'EntityExtractionTask',
                        component: NLPTask,
                        isChildren: true,
                        typeCode: 'EET'
                    },
                    {
                        title: '关系抽取',
                        key: 'RelationalExtractionTask',
                        component: NLPTask,
                        isChildren: true,
                        typeCode: 'RET'
                    }, {
                        title: '多类别分类',
                        key: 'CategoriesClassifyTask',
                        component: NLPTask,
                        isChildren: true,
                        typeCode: 'CCT'
                    }, {
                        title: '多标签分类',
                        key: 'MultitagClassifyTask',
                        component: NLPTask,
                        isChildren: true,
                        typeCode: 'MCT'
                    },
                        //  {
                        //     title: 'Demo',
                        //     key: 'Demo',
                        //     component: Demo,
                        //     isChildren: true,
                        //     typeCode: 'Demo'
                        // }
                    ]
                },
                {
                    title: '系统管理',
                    key: 'SystemMng',
                    children: [{
                        title: '人员管理',
                        key: 'MembersManagement',
                        component: MembersManagement,
                        isChildren: true
                    }
                        // ,
                        //  {
                        //     title: '团队管理',
                        //     key: 'TeamManagement',
                        //     component: TeamManagement,
                        //     isChildren: true
                        // }
                    ]
                }],
            num: 0,
            current: '',
            userName: '',
            openKeys: [],
            tempOpenKeys: []
        };
    }



    componentDidMount = () => {
        const { menuList } = this.state
        const userName = localStorage.getItem('userName')
        if (userName) {
            this.setState({
                userName
            })
        }
        const len = menuList.length
        for (let index = 0; index < len; index++) {
            if (menuList[index].children) {
                for (let index2 = 0; index2 < menuList[index].children.length; index2++) {
                    if (window.location.href.indexOf(menuList[index].children[index2].key) > -1) {
                        this.setState({
                            current: menuList[index].children[index2].key
                        }, () => {
                            this.getParentKeyFun()
                        })
                        break;
                    }
                }
            } else {
                if (window.location.href.indexOf(menuList[index].key) > -1) {
                    this.setState({
                        current: menuList[index].key
                    }, () => {
                        this.getParentKeyFun()
                    })
                    break;
                }
            }
        }

    }


    getParentKeyFun = () => {
        let openkeys = [];
        const getParentKey = (key, tree) => {
            let parentKey;
            for (let i = 0; i < tree.length; i++) {
                const node = tree[i];
                if (node.children) {
                    if (node.children.some(item => {
                        return item.key === key
                    })) {
                        parentKey = node.key;
                    } else if (getParentKey(key, node.children)) {
                        parentKey = getParentKey(key, node.children);
                    }
                }
            }
            return parentKey;
        }
        const getAllParent = (key) => {
            const parentKey = getParentKey(key, this.state.menuList);
            if (parentKey) {
                openkeys.push(parentKey);
                getAllParent(parentKey)
            }
        }
        getAllParent(this.state.current);
        this.setState({
            openKeys: openkeys,
            tempOpenKeys: JSON.parse(JSON.stringify(openkeys))
        })
    }

    onOpenChange = (openKeys) => {
        this.setState({
            openKeys,
            tempOpenKeys: openKeys.length > 0 ? JSON.parse(JSON.stringify(openKeys)) : this.state.tempOpenKeys
        }, () => {
        })
    }

    toggle = () => {
        this.setState({
            openKeys: []
        }, () => {
            this.setState({
                openKeys: this.state.tempOpenKeys,
                collapsed: !this.state.collapsed,
            });
        })

    };

    renderIcon = (title) => {
        switch (title) {
            case '首页':
                return <HomeOutlined />
            case '数据管理':
                return <DatabaseOutlined />
            case '任务管理':
                return <CarryOutOutlined />
            case '任务执行':
                return <ShareAltOutlined />
            case '知识应用':
                return <DeploymentUnitOutlined />
            case '系统管理':
                return <SettingOutlined />
            case '图谱应用':
                return <ApartmentOutlined />
            case '自然语言':
                return <RobotOutlined />
        }
    }


    renderMenu = (menuList) => {
        const { collapsed } = this.state
        const menueResult = menuList.map(item => {
            if (item.children) {
                if (this.props.roleId.indexOf('admin') < 0 && item.title === '系统管理') {
                    return
                } else {
                    return (
                        <SubMenu
                            key={item.key}
                            title={
                                <div className="mds-siderMenu-ripplesWrap">
                                    <Ripples
                                        className="mds-siderMenu-ripples"
                                    >
                                        {this.renderIcon(item.title)}
                                        <span className='mds-siderMenu-title'>
                                            {
                                                item.children
                                                    ?
                                                    item.title
                                                    :
                                                    <Link key={item.key} to={`/HKKS/${item.key}`}>{item.title}</Link>
                                            }

                                        </span>
                                    </Ripples>
                                </div>
                            }
                        >
                            {
                                this.renderMenu(item.children)
                            }
                        </SubMenu>
                    )
                }
            } else {
                return (
                    <Menu.Item key={item.key}>
                        <div className="mds-siderMenu-ripplesWrap">
                            <Ripples
                                className="mds-siderMenu-ripples"
                            >
                                {item.isChildren ? null : this.renderIcon(item.title)}
                                <span className={!item.isChildren ? 'mds-siderMenu-title' : 'mds-siderMenu-title mds-siderMenu-titleChildren'}>
                                    <Link key={item.key} to={`/HKKS/${item.key}`}>{item.title}</Link>
                                </span>
                            </Ripples>
                        </div>
                    </Menu.Item>
                )
            }
        })
        return menueResult
    }

    renderRouter = (menuList, item2) => {
        return menuList.map(item => {
            if (item.children) {
                return this.renderRouter(item.children, item)
            } else {
                if (this.props.roleId.indexOf('admin') < 0 && item2 && item2.title === '系统管理') {
                    return (
                        <Route key={item.key} path={`/HKKS/${item.key}`} render={() => {
                            return <Res {...item} />
                        }} />
                    )
                } else {
                    const { component: Component } = item
                    return (
                        <Route key={item.key} path={`/HKKS/${item.key}`} render={() => {
                            return <Component {...item} />
                        }} />
                    )
                }
            }
        })
    }

    onCollapse = (collapsed, type) => {
        if (collapsed) {
            this.setState({
                openKeys: []
            }, () => {
                this.setState({
                    collapsed,
                    openKeys: this.state.tempOpenKeys
                })
            })
        }

    }

    handleClick = e => {
        this.setState({
            current: e.key,
        });
    };

    render() {
        const { menuList, collapsed, current, openKeys } = this.state
        const { userName } = this.props
        return (
            <Fragment>
                <Layout style={{ height: '100%' }}>
                    <LayoutHeader />
                    <Layout className='mds-layout-wrap'>
                        <Sider
                            trigger={null}
                            collapsible
                            collapsed={collapsed}
                            breakpoint='lg'
                            theme='light'
                            onCollapse={(collapsed, type) => this.onCollapse(collapsed, type)}
                        >
                            <div className='mds-layout-peopleInfoWrap'>
                                <Icon
                                    component={() => {
                                        return <svg t="1594608212614" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3716" width="1em" height="1em"><path d="M512 512m-512 0a512 512 0 1 0 1024 0 512 512 0 1 0-1024 0Z" fill="#424A60" p-id="3717"></path><path d="M934.523586 801.121103C922.924138 688.286897 827.603862 600.275862 711.697655 600.275862h-104.977655A24.09931 24.09931 0 0 1 582.62069 576.176552v-11.387586c0-10.292966 6.69131-19.102897 16.331034-22.722207 102.470621-38.523586 172.632276-206.636138 158.384552-325.437793C743.883034 104.500966 652.711724 14.141793 540.495448 1.588966a251.851034 251.851034 0 0 0-27.100689-1.553656l-0.847449-0.017655C375.790345-0.282483 264.827586 110.486069 264.827586 247.172414c0 106.354759 67.213241 260.502069 161.456552 295.353379 9.233655 3.407448 15.095172 12.588138 15.095172 22.439724v11.211035c0 13.312-10.78731 24.09931-24.09931 24.09931h-104.977655c-115.906207 0-211.226483 88.011034-222.825931 200.845241C181.72469 935.688828 336.525241 1024 512 1024s330.27531-88.311172 422.523586-222.878897z" fill="#FBCE9D" p-id="3718"></path><path d="M591.307034 116.70069c65.588966 18.025931 127.346759 58.368 166.894345 111.616-0.282483-3.901793-0.406069-7.891862-0.865103-11.70538C743.883034 104.500966 652.711724 14.141793 540.495448 1.588966a251.851034 251.851034 0 0 0-27.100689-1.553656l-0.847449-0.017655c-122.173793-0.264828-223.514483 88.187586-243.78262 204.499862h0.088276c3.160276 4.449103 6.249931 8.951172 9.622068 13.24138 1.235862-1.536 2.489379-3.054345 3.760552-4.555035 35.310345-42.01931 94.296276-51.74731 144.472276-29.501793a123.586207 123.586207 0 0 0 164.599172-67.001379z" fill="#6C797A" p-id="3719"></path><path d="M934.523586 801.121103C922.924138 688.286897 827.586207 600.275862 711.697655 600.275862H688.551724l-105.931034 105.931035h-141.24138l-52.965517-35.310345-60.539586-70.62069h-15.571862c-115.888552 0-211.226483 88.011034-222.825931 200.845241C181.72469 935.688828 336.525241 1024 512 1024s330.27531-88.311172 422.523586-222.878897z" fill="#E7ECED" p-id="3720"></path><path d="M759.172414 960.300138c12.093793-6.69131 23.834483-13.929931 35.310345-21.53931V829.793103h-35.310345v130.507035zM264.827586 960.300138V829.793103h-35.310345v108.967725c11.475862 7.609379 23.216552 14.848 35.310345 21.53931z" fill="#CCD5D6" p-id="3721"></path><path d="M459.034483 670.896552h105.931034v105.931034h-105.931034z" fill="#38454F" p-id="3722"></path><path d="M440.690759 1018.950621c23.304828 3.248552 47.104 5.049379 71.309241 5.049379 24.399448 0 48.357517-1.818483 71.838897-5.12L547.310345 776.827586h-70.62069l-35.998896 242.123035z" fill="#546A79" p-id="3723"></path><path d="M459.034483 685.02069L388.413793 600.275862h-85.733517a1.730207 1.730207 0 0 0-1.606621 2.365793L370.758621 776.827586l88.275862-58.844689V685.02069zM721.319724 600.275862H635.586207l-70.62069 84.744828v32.962207L653.241379 776.827586l69.667311-174.185931a1.712552 1.712552 0 0 0-1.588966-2.365793z" fill="#FFFFFF" p-id="3724"></path></svg>
                                    }}
                                    style={{
                                        fontSize: "60px",
                                        marginRight: '5px'
                                    }}
                                />
                                <span>{this.state.userName ? this.state.userName : userName}</span>
                            </div>
                            <Menu
                                className='mds-siderMenu-wrap'
                                mode="inline"
                                onClick={this.handleClick}
                                onOpenChange={this.onOpenChange}
                                openKeys={openKeys}
                                // defaultSelectedKeys={['DataSourceMng']}
                                selectedKeys={[current]}
                            >
                                {this.renderMenu(menuList)}
                            </Menu>
                            {React.createElement(this.state.collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
                                className: 'trigger',
                                onClick: this.toggle,
                            })}
                        </Sider>
                        <Layout className="site-layout">
                            <Content
                                className="site-layout-background"
                                style={{
                                    margin: '24px 16px',
                                    minHeight: 'unset',
                                    // overflow: 'hidden'
                                }}
                            >
                                {this.renderRouter(this.state.menuList)}
                            </Content>
                        </Layout>
                    </Layout>
                </Layout>
            </Fragment>
        );
    }
}

const mapStateToProps = (state) => {
    return { ...state.LoginReducer }
}

const mapDispatchToProps = (dispatch) => {
    return {}
}

export default connect(mapStateToProps, mapDispatchToProps)(LayoutWrap)