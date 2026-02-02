import '../../styles/mainPortal/Main.css'

export default function Main() {
    return (
        <div>
            <main>
                <section id="Redmine" className="Main">
                    <h2>Redmine</h2>
                    <p>Все ссылки на задачи</p>
                    <div className="suppliers-grid">
                        <a href="https://it-social.ru" target="_blank" className="supplier-card" id="RedMain">
                            <div className="supplier-content">
                                <span className="supplier-name">Главная страница</span>
                            </div>
                        </a>
                        <a href="https://it-social.ru/projects/apt/issues" target="_blank" className="supplier-card" id="IT">
                            <div className="supplier-content">
                                <span className="supplier-name">Задачи IT</span>
                            </div>
                        </a>
                        <a href="https://it-social.ru/projects/aho/issues" target="_blank" className="supplier-card" id="AXO">
                            <div className="supplier-content">
                                <span className="supplier-name">Задачи АХО</span>
                            </div>
                        </a>
                        <a href="https://it-social.ru/projects/apt/wiki" target="_blank" className="supplier-card" id="RedWiki">
                            <div className="supplier-content">
                                <span className="supplier-name">Wiki Redmine</span>
                            </div>
                        </a>
                    </div>
                </section>
                <section id="Saby" className="Main">
                    <h2>СБИС</h2>
                    <p>Все ссылки электроного документооборота</p>
                    <div className="suppliers-grid">
                        <a href="https://sso.sbis.ru/auth-online/?ret=online.saby.ru/page/saby-app?add_gtm=true" target="_blank" className="supplier-card">
                            <div className="supplier-content">
                                <span className="supplier-name">Вход в СБИС</span>
                            </div>
                        </a>
                        <a href="https://online.saby.ru/page/documents-incoming" target="_blank" className="supplier-card">
                            <div className="supplier-content">
                                <span className="supplier-name">Входящие документы</span>
                            </div>
                        </a>
                        <a href="https://online.saby.ru/page/tasks-process" target="_blank" className="supplier-card">
                            <div className="supplier-content">
                                <span className="supplier-name">Товарный отчет</span>
                            </div>
                        </a>
                        <a href="https://online.saby.ru/page/money-cash-payments" target="_blank" className="supplier-card">
                            <div className="supplier-content">
                                <span className="supplier-name">Касса</span>
                            </div>
                        </a>
                    </div>
                </section>
                <section id="Suppliers" className="Main">
                    <h2>Поставщики</h2>
                    <p>Список сайтов всех поставщиков</p>
                    <div className="suppliers-grid">
                        {/* <!-- Катрен --> */}
                        <a href="https://katren.ru/" target="_blank" className="supplier-card" id="katre">
                            <div className="supplier-content">
                                <span className="supplier-name">Катрен</span>
                            </div>
                        </a>
                        {/* <!-- Протек --> */}
                        <a href="https://protek.ru" target="_blank" className="supplier-card" id="protek">
                            <div className="supplier-content">
                                <span className="supplier-name">Протек</span>
                            </div>
                        </a>
                        {/* <!-- Гранд Капитал --> */}
                        <a href="https://grand-capital.ru" target="_blank" className="supplier-card" id="grandcapital">
                            <div className="supplier-content">
                                <span className="supplier-name">Гранд Капитал</span>
                            </div>
                        </a>
                        {/* <!-- Пульс --> */}
                        <a href="https://puls.ru" target="_blank" className="supplier-card" id="puls">
                            <div className="supplier-content">
                                <span className="supplier-name">Пульс</span>
                            </div>
                        </a>
                        {/* <!-- Фармкомплект --> */}
                        <a href="https://www.pharmk.ru" target="_blank" className="supplier-card" id="farmkomplekt">
                            <div className="supplier-content">
                                <span className="supplier-name">Фармкомплект</span>
                            </div>
                        </a>
                        <a href="http://astifarm.ru" target="_blank" className="supplier-card" id="alenfarma">
                            <div className="supplier-content">
                                <span className="supplier-name">Аленфарма</span>
                            </div>
                        </a>
                        <a href="https://agrores.ru" target="_blank" className="supplier-card" id="agroresurs">
                            <div className="supplier-content">
                                <span className="supplier-name">Агроресурсы</span>
                            </div>
                        </a>
                        <a href="https://bsspharm.ru" target="_blank" className="supplier-card" id="BSS">
                            <div className="supplier-content">
                                <span className="supplier-name">БСС</span>
                            </div>
                        </a>
                    </div>
                </section>
                <section id="Wiki" className="Main">
                    <h2>Полезные ссылки</h2>
                    <p>Ссылки на справочники лекарств и другие ресурсы</p>
                    <div className="suppliers-grid">
                        <a href="https://www.rlsnet.ru/" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">РЛС</span>
                            </div>
                        </a>
                        <a href="http://ref003.ru/" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">Справка 003 Ростовская область</span>
                            </div>
                        </a>
                        <a href="https://www.vidal.ru/" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">Видаль</span>
                            </div>
                        </a>
                        <a href="https://roszdravnadzor.gov.ru/" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">Росздравнадзор</span>
                            </div>
                        </a>
                        <a href="https://www.rospotrebnadzor.ru/" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">Роспотребнадзор</span>
                            </div>
                        </a>
                        <a href="https://classNameifikators.ru/okpd" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">ОКПД-2</span>
                            </div>
                        </a>
                        <a href="https://grls.rosminzdrav.ru/pricelims.aspx" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">ГРЛС</span>
                            </div>
                        </a>
                        <a href="https://social-apteka.ru" target="_blank" className="supplier-card" id="">
                            <div className="supplier-content">
                                <span className="supplier-name">Социальная аптека</span>
                            </div>
                        </a>
                        <a href="https://wiki.farmp.ru" target="_blank" className="supplier-card" id="FarmpWiki">
                            <div className="supplier-content">
                                <span className="supplier-name">Wiki ГК "Фармацевт"</span>
                            </div>
                        </a>
                    </div>
                </section>
                <section id="Additional" className="Main">
                    <h2>Дополнительные сервисы</h2>
                    <p>Все дополнительные сервисы и ссылки</p>
                    <div className="suppliers-grid">
                        <a href="https://social-apteka.mirapolis.ru" target="_blank" className="supplier-card" id="miropolis">
                            <div className="supplier-content">
                                <span className="supplier-name">Мираполис</span>
                            </div>
                        </a>
                        <a href="https://encashment.sberbank.ru/" target="_blank" className="supplier-card" id="sberinkas">
                            <div className="supplier-content">
                                <span className="supplier-name">СберИнкасация</span>
                            </div>
                        </a>
                        <a href="http://kk.farmp.ru:8080/ords/f?p=250" target="_blank" className="supplier-card" id="cashbook">
                            <div className="supplier-content">
                                <span className="supplier-name">Кассовая книга</span>
                            </div>
                        </a>
                    </div>
                </section>
                <section id="OnlineOrders" className="Main">
                    <h2>Дополнительные ссылки</h2>
                    <p>Ссылки на дополнительные сайты</p>
                    <div className="suppliers-grid">
                        <a href="https://apteka.ru" target="_blank" className="supplier-card" id="Aptekaru">
                            <div className="supplier-content">
                                <span className="supplier-name">АптекаРу</span>
                            </div>
                        </a>
                        <a href="https://zdravcity.ru" target="_blank" className="supplier-card" id="Zdravsity">
                            <div className="supplier-content">
                                <span className="supplier-name">Здравсити</span>
                            </div>
                        </a>
                        <a href="https://www.rigla.ru" target="_blank" className="supplier-card" id="Rigla">
                            <div className="supplier-content">
                                <span className="supplier-name">Ригла</span>
                            </div>
                        </a>
                        <a href="https://vitaexpress.ru" target="_blank" className="supplier-card" id="Vita">
                            <div className="supplier-content">
                                <span className="supplier-name">Вита</span>
                            </div>
                        </a>
                        <a href="https://apteka-april.ru" target="_blank" className="supplier-card" id="Aprel">
                            <div className="supplier-content">
                                <span className="supplier-name">Апрель</span>
                            </div>
                        </a>
                        <a href="https://planetazdorovo.ru" target="_blank" className="supplier-card" id="Planeta">
                            <div className="supplier-content">
                                <span className="supplier-name">Планета Здоровья</span>
                            </div>
                        </a>
                    </div>
                </section>
            </main>
        </div>
    )
}