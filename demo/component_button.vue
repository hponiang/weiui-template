<template>
    <div class="app">

        <weiui_navbar class="navbar">
            <weiui_navbar_item type="back"></weiui_navbar_item>
            <weiui_navbar_item type="title">
                <text class="title">常用按钮</text>
            </weiui_navbar_item>
            <weiui_navbar_item type="right" @click="viewCode('component/weiui_button')">
                <weiui_icon content="code-working" class="iconr"></weiui_icon>
            </weiui_navbar_item>
        </weiui_navbar>

        <scroller class="list">

            <text class="list-title">预设样式</text>

            <div class="list-item">
                <weiui_button class="button" :weiui="{text:'默认'}"></weiui_button>
                <weiui_button class="button" :weiui="{text:'红色', model:'red'}"></weiui_button>
            </div>

            <div class="list-item">
                <weiui_button class="button" :weiui="{text:'绿色', model:'green'}"></weiui_button>
                <weiui_button class="button" :weiui="{text:'蓝色', model:'blue'}"></weiui_button>
            </div>

            <div class="list-item">
                <weiui_button class="button" :weiui="{text:'粉红', model:'pink'}"></weiui_button>
                <weiui_button class="button" :weiui="{text:'黄色', model:'yellow'}"></weiui_button>
            </div>

            <div class="list-item">
                <weiui_button class="button" :weiui="{text:'橙色', model:'orange'}"></weiui_button>
                <weiui_button class="button" :weiui="{text:'灰色', model:'gray'}"></weiui_button>
            </div>

            <div class="list-item">
                <weiui_button class="button" :weiui="{text:'黑色', model:'black'}"></weiui_button>
                <weiui_button class="button" :weiui="{text:'白色', model:'white'}"></weiui_button>
            </div>

            <div class="list-item">
                <weiui_button class="button" :weiui="{text:'加载中', loading:true}"></weiui_button>
                <weiui_button class="button" :weiui="{text:'禁用', disabled:true}"></weiui_button>
            </div>

            <text class="list-title">自定义样式</text>

            <div class="list-input-item">
                <text class="cell">按钮文字：</text>
                <input class="input" placeholder="文字" v-model="text"/>
            </div>

            <div class="list-input-item">
                <text class="cell" style="flex:1">是否加载中：</text>
                <switch class="switch" :checked="loading" @change="loading = !loading"></switch>
            </div>

            <div class="list-input-item">
                <text class="cell" style="flex:1">是否禁用：</text>
                <switch class="switch" :checked="disabled" @change="disabled = !disabled"></switch>
            </div>

            <div class="list-input-item">
                <text class="cell">按钮宽度：</text>
                <input class="input" placeholder="自定义样式实现, 默认全屏"
                       v-model="width"/>
            </div>

            <div class="list-input-item">
                <text class="cell">按钮高度：</text>
                <input class="input" placeholder="自定义样式实现, 默认高度80px" v-model="height"/>
            </div>

            <div class="list-input-item">
                <text class="cell">背景颜色：</text>
                <input class="input" placeholder="自定义样式实现" v-model="backgroundColor"/>
            </div>

            <div class="list-input-item">
                <text class="cell">边框颜色：</text>
                <input class="input" placeholder="自定义样式实现" v-model="borderColor"/>
            </div>

            <div class="list-input-item">
                <text class="cell">边框圆角：</text>
                <input class="input" placeholder="自定义样式实现,默认8px" v-model="borderRadius"/>
            </div>

            <div class="demo-item">
                <weiui_button class="demoButton" :weiui="weiuiStyle" :style="demoStyle"></weiui_button>
            </div>

        </scroller>


    </div>
</template>

<style scoped>
    .app {
        width: 750px;
        flex: 1;
        background-color: #ffffff;
    }

    .navbar {
        width: 750px;
        height: 100px;
    }

    .title {
        font-size: 28px;
        color: #ffffff
    }

    .iconr {
        width: 100px;
        height: 100px;
        color: #ffffff;
    }

    .list {
        width: 750px;
        flex: 1;
    }

    .list-title {
        margin-top: 24px;
        margin-bottom: 12px;
        padding-top: 36px;
        padding-right: 24px;
        padding-bottom: 24px;
        padding-left: 24px;
        font-size: 28px;
        color: #757575;
    }

    .list-item {
        width: 750px;
        flex-direction: row;
    }

    .button {
        font-size: 24px;
        margin-left: 37.5px;
        margin-right: 37.5px;
        margin-bottom: 20px;
        width: 300px;
        height: 80px;
    }

    .list-input-item {
        width: 750px;
        height: 90px;
        flex-direction: row;
        align-items: center;
        border-bottom-color: #e4e4e4;
        border-bottom-style: solid;
        border-bottom-width: 1px;
    }

    .cell {
        padding-left: 50px;
        font-size: 24px;
        color: #666666;
    }

    .switch {
        margin-right: 40px;
    }

    .input {
        padding-right: 50px;
        flex: 1;
        height: 90px;
        text-align: right;
        font-size: 28px;
    }

    .demo-item {
        width: 750px;
        padding-top: 30px;
        padding-bottom: 30px;
        align-items: center;
    }

</style>

<script>
    import {openViewCode} from "../statics/js/app";

    const weiui = weex.requireModule('weiui');

    export default {
        data() {
            return {
                text: '按钮',
                width: '702px',
                height: '80px',
                loading: false,
                disabled: false,
                backgroundColor: '#FF5000',
                borderColor: '#FF5000',
                borderRadius: '8px',
                fontSize: '28px',
                color: '#FFFFFF'
            };
        },
        mounted() {
            this.$nextTick(() => {
                this.text = '按钮文字';
            });
        },
        computed: {
            weiuiStyle() {
                const { backgroundColor, borderColor, borderRadius, text, color, loading, disabled } = this;
                const customStyle = {};
                if (backgroundColor) {
                    customStyle.backgroundColor = backgroundColor;
                }
                if (borderColor) {
                    customStyle.borderColor = borderColor;
                    customStyle.borderWidth = "1px";
                }
                if (borderRadius) {
                    customStyle.borderRadius = borderRadius;
                }
                if (text) {
                    customStyle.text = text;
                }
                if (color) {
                    customStyle.color = color;
                }
                customStyle.loading = loading;
                customStyle.disabled = disabled;
                return customStyle;
            },

            demoStyle() {
                const { width, height } = this;
                const customStyle = {};
                customStyle.width = width;
                customStyle.height = height;
                return customStyle;
            }
        },
        methods: {
            viewCode(str) {
                openViewCode(str);
            },
        }
    };
</script>
