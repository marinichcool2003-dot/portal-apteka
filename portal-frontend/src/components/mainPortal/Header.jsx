import logo from '../../assets/static-images/main-portal-icons/logo.svg'

import '../../styles/mainPortal/Header.css'

export default function Header() {
    return (
        <header>
            <a href="#Startova" className="logo">
                <div className="logo-bg">
                    <object type="image/svg+xml" data={logo} width="140" height="40"></object>
                </div>
            </a>

            <nav>
                <ul>
                    <li><a href="#Redmine">Redmine</a></li>
                    <li><a href="#Saby">СБИС</a></li>
                    <li className="has-submenu"><a href="#Suppliers">Поставщики</a>
                        <ul>
                            <li><a href="#Suppliers">Катрен</a></li>
                            <li><a href="#Suppliers">Протек</a></li>
                            <li><a href="#Suppliers">Гранд капитал</a></li>
                            <li><a href="#Suppliers">Пульс</a></li>
                            <li><a href="#Suppliers">Фармкомплект</a></li>
                            <li><a href="#Suppliers">Аленфарма</a></li>
                            <li><a href="#Suppliers">Агроресурсы</a></li>
                            <li><a href="#Suppliers">БСС</a></li>
                        </ul>
                    </li>

                    <li><a href="#Wiki">Полезные ссылки</a></li>
                    <li className="has-submenu"><a href="#Additional">Доп. сервисы</a>
                        <ul>
                            <li><a href="#Additional">Мираполис</a></li>
                            <li><a href="#Additional">СберИнкасация</a></li>
                            <li><a href="#KK">Кассовая книга</a></li>
                        </ul>
                    </li>
                    <li><a href="#OnlineOrders">Доп. сслыки</a></li>
                </ul>
            </nav>

            {/* <!-- Бургер-меню --> */}
            <div className="burger-menu">
                <span></span>
                <span></span>
                <span></span>
            </div>

            {/* <!-- Боковая панель --> */}
            <div className="side-panel">
                <ul>
                    <li><a href="#Redmine">Redmine</a></li>
                    <li><a href="#Saby">СБИС</a></li>
                    <li className="has-submenu"><a href="#Suppliers">Поставщики</a>
                        <ul>
                            <li><a href="#Suppliers">Катрен</a></li>
                            <li><a href="#Suppliers">Протек</a></li>
                            <li><a href="#Suppliers">Гранд капитал</a></li>
                            <li><a href="#Suppliers">Пульс</a></li>
                            <li><a href="#Suppliers">Фармкомплект</a></li>
                            <li><a href="#Suppliers">Аленфарма</a></li>
                            <li><a href="#Suppliers">Агроресурсы</a></li>
                            <li><a href="#Suppliers">БСС</a></li>
                        </ul>
                    </li>

                    <li><a href="#Wiki">Полезные ссылки</a></li>
                    <li className="has-submenu"><a href="#Additional">Доп. сервисы</a>
                        <ul>
                            <li><a href="#Additional">Мираполис</a></li>
                            <li><a href="#Additional">СберИнкасация</a></li>
                            <li><a href="#KK">Кассовая книга</a></li>
                        </ul>
                    </li>
                    <li><a href="#OnlineOrders">Интернет заказы</a></li>
                </ul>
            </div>

            {/* <!-- Затемнение фона --> */}
            <div className="overlay"></div>
        </header>
    )
}