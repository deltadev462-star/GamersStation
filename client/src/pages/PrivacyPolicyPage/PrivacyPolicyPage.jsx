import React from 'react';
import { useTranslation } from 'react-i18next';
import { Shield, FileText, Users, Package, CreditCard, Truck, RotateCcw, UserCheck, Ban, Lock, Database, Eye, AlertTriangle } from 'lucide-react';
import Header from '../../components/Header/Header';
import Footer from '../../components/Footer/Footer';
import SEO from '../../components/SEO/SEO';
import './PrivacyPolicyPage.css';

const PrivacyPolicyPage = () => {
  const { t } = useTranslation();

  const sections = [
    {
      id: 'terms',
      title: t('privacyPolicy.terms.title'),
      icon: <FileText size={24} />,
      items: [
        {
          id: 'nature',
          icon: <Shield size={20} />,
          title: t('privacyPolicy.terms.nature.title'),
          content: t('privacyPolicy.terms.nature.content'),
        },
        {
          id: 'eligibility',
          icon: <UserCheck size={20} />,
          title: t('privacyPolicy.terms.eligibility.title'),
          content: t('privacyPolicy.terms.eligibility.content'),
        },
        {
          id: 'responsibility',
          icon: <Users size={20} />,
          title: t('privacyPolicy.terms.responsibility.title'),
          content: t('privacyPolicy.terms.responsibility.content'),
        },
        {
          id: 'evaluation',
          icon: <Package size={20} />,
          title: t('privacyPolicy.terms.evaluation.title'),
          content: t('privacyPolicy.terms.evaluation.content'),
        },
        {
          id: 'payment',
          icon: <CreditCard size={20} />,
          title: t('privacyPolicy.terms.payment.title'),
          content: t('privacyPolicy.terms.payment.content'),
        },
        {
          id: 'shipping',
          icon: <Truck size={20} />,
          title: t('privacyPolicy.terms.shipping.title'),
          content: t('privacyPolicy.terms.shipping.content'),
        },
        {
          id: 'returns',
          icon: <RotateCcw size={20} />,
          title: t('privacyPolicy.terms.returns.title'),
          content: t('privacyPolicy.terms.returns.content'),
        },
        {
          id: 'sellerObligations',
          icon: <UserCheck size={20} />,
          title: t('privacyPolicy.terms.sellerObligations.title'),
          content: t('privacyPolicy.terms.sellerObligations.content'),
        },
        {
          id: 'prohibited',
          icon: <Ban size={20} />,
          title: t('privacyPolicy.terms.prohibited.title'),
          content: t('privacyPolicy.terms.prohibited.content'),
        },
      ],
    },
    {
      id: 'privacy',
      title: t('privacyPolicy.privacy.title'),
      icon: <Lock size={24} />,
      items: [
        {
          id: 'dataCollection',
          icon: <Database size={20} />,
          title: t('privacyPolicy.privacy.dataCollection.title'),
          content: t('privacyPolicy.privacy.dataCollection.content'),
        },
        {
          id: 'requiredData',
          icon: <FileText size={20} />,
          title: t('privacyPolicy.privacy.requiredData.title'),
          content: t('privacyPolicy.privacy.requiredData.content'),
        },
        {
          id: 'dataUse',
          icon: <Eye size={20} />,
          title: t('privacyPolicy.privacy.dataUse.title'),
          content: t('privacyPolicy.privacy.dataUse.content'),
        },
        {
          id: 'reporting',
          icon: <AlertTriangle size={20} />,
          title: t('privacyPolicy.privacy.reporting.title'),
          content: t('privacyPolicy.privacy.reporting.content'),
        },
      ],
    },
  ];

  return (
    <>
      <SEO
        title={t('privacyPolicy.pageTitle')}
        description={t('privacyPolicy.pageDescription')}
        type="website"
      />
      <div className="privacy-policy-page">
        <Header />
        <main className="privacy-policy-main">
          {/* Hero Section */}
          <div className="privacy-hero">
            <div className="privacy-hero-container">
              <div className="privacy-hero-icon">
                <Shield size={40} />
              </div>
              <h1 className="privacy-hero-title">{t('privacyPolicy.heroTitle')}</h1>
              <p className="privacy-hero-subtitle">{t('privacyPolicy.heroSubtitle')}</p>
            </div>
          </div>

          {/* Content */}
          <div className="privacy-content">
            <div className="privacy-container">
              {sections.map((section) => (
                <div key={section.id} className="privacy-section">
                  <div className="privacy-section-header">
                    <span className="privacy-section-icon">{section.icon}</span>
                    <h2 className="privacy-section-title">{section.title}</h2>
                  </div>
                  <div className="privacy-items">
                    {section.items.map((item, index) => (
                      <div key={item.id} className="privacy-item">
                        <div className="privacy-item-header">
                          <span className="privacy-item-number">{index + 1}</span>
                          <span className="privacy-item-icon">{item.icon}</span>
                          <h3 className="privacy-item-title">{item.title}</h3>
                        </div>
                        <div className="privacy-item-content">
                          <p>{item.content}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </main>
        <Footer />
      </div>
    </>
  );
};

export default PrivacyPolicyPage;
