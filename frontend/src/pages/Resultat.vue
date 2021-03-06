<template>
  <section class="md-layout">
    <div class="md-layout-item">
      <result-sort
        class="sort-container"
        :sort="sortBy"
        :order="orderBy"
        :tags="searchTagList"
        @change:sort="sortBy = $event; updateSearchURL('sort', $event)"
        @change:order="orderBy = $event; updateSearchURL('order', $event)"
        @tag:remove="removeTagFromSearchList($event)"
      ></result-sort>
    </div>
    <div class="md-layout-item md-size-80">
      <div class="loading-search-results" v-if="loading">
        <md-progress-bar md-mode="indeterminate"/>
      </div>
      <div class="results-container">
        <md-tabs
          md-alignment="fixed"
          :md-active-tab="activeTab"
          @md-changed="activeTab = $event; updateSearchURL('tab', $event)"
        >
          <template slot="md-tab" slot-scope="{ tab }">
            {{ tab.label }}
            <i
              class="badge"
              v-if="tab.data.size"
              :title="tab.data.size | pluralize(' result')"
            >{{ tab.data.size }}</i>
          </template>
          <md-tab
            id="sotas"
            md-label="States Of The Art"
            md-icon="view_module"
            :md-template-data="badgeNumber('sotas')"
          >
            <sota-list v-show="!loading" :list="results.sotas"></sota-list>
            <md-empty-state
              v-if="!loading && (!results.sotas || results.sotas.length == 0)"
              md-icon="sentiment_dissatisfied"
              md-label="No states of the art found"
              :md-description="'Sorry, we didn\'t find any SoTA matching your search for \'\''+searchTerm+'\'\''"
            >
              <div>If you want to try again,</div>
              <ul>
                <li>
                  Check your spelling,
                  <md-icon>insert_emoticon</md-icon>
                </li>
                <li>Use less specific term(s) for a wider result</li>
                <li>Use differents words with the same meaning</li>
              </ul>
            </md-empty-state>
          </md-tab>
          <md-tab
            id="articles"
            md-label="Articles"
            md-icon="description"
            :md-template-data="badgeNumber('articles')"
          >
            <md-empty-state
              v-if="!loading &&  (!results.articles || results.articles.length == 0)"
              md-icon="description"
              md-label="No articles found"
              :md-description="articleEmptyStateMsg"
            ></md-empty-state>
            <article-list
              v-else
              v-show="!loading"
              :list="results.articles"
              :meta="metas['articles']"
              :hasPagination="isPaginationVisible('articles')"
              @pagination="page = $event; updateSearchURL('page', $event) "
            ></article-list>
          </md-tab>
          <md-tab
            id="authors"
            md-label="Authors/Users"
            md-icon="people"
            :md-template-data="badgeNumber('users')"
          >
            <author-list v-show="!loading" :list="results.users"></author-list>
            <author-list v-show="!loading" :list="results.authors"></author-list>
            <md-empty-state
              v-if="!loading && (!results.users || results.users.length == 0 && results.authors.length == 0)"
              md-icon="people"
              md-label="No authors/users found"
              :md-description="'Sorry, we didn\'t find any authors/users matching your search for \'\''+searchTerm+'\'\''"
            ></md-empty-state>
          </md-tab>
          <md-tab
            id="graphics"
            md-label="Graphics"
            md-icon="share"
            :md-disabled="articlesTitles.length == 0"
          >
            <md-empty-state
              v-if="!loading && articlesTitles.length == 0"
              md-icon="share"
              md-label="No graphics to display"
              md-description="Try another search"
            ></md-empty-state>
            <graphics v-else :articles-titles="articlesTitles" :linked-articles="relatedArticles"/>
          </md-tab>
          <md-tab
            id="wordcloud"
            md-label="WordCloud"
            :md-icon="isEmptyArticlesTags ? 'cloud_off' : 'cloud'"
            :md-disabled="isEmptyArticlesTags"
          >
            <md-empty-state
              v-if="!loading && isEmptyArticlesTags"
              md-icon="cloud"
              md-label="No word cloud to display"
            ></md-empty-state>
            <wordcloud
              v-else
              :data="tagList"
              nameKey="name"
              valueKey="value"
              :color="myColors"
              :showTooltip="false"
              :fontSize="[13,40]"
              :wordClick="wordClickHandler"
              :rotate="{from: 0, to: 0, numOfOrientation: 5 }"
            ></wordcloud>
            <md-snackbar :md-active.sync="showSnackbar" :md-duration="4000">
              <span>{{snackMsgWc}}</span>
            </md-snackbar>
          </md-tab>
        </md-tabs>
      </div>
    </div>
  </section>
</template>

<script>
import SortOptions from "@/components/resultat/SortOptions";
import SotaList from "@/components/resultat/SotaList";
import AuthorList from "@/components/resultat/AuthorList";
import ArticleList from "@/components/resultat/ArticleList";
import Graphics from "@/components/resultat/Graphics";
import wordcloud from "vue-wordcloud";

import { getSearchResults, getTags } from "@/services/api";

export default {
  name: "Resultat",
  components: {
    "result-sort": SortOptions,
    SotaList,
    AuthorList,
    ArticleList,
    Graphics,
    wordcloud
  },
  data() {
    return {
      loading: false,
      changingTab: false,
      searchTerm: null,
      sortBy: this.$route.query["sort"] || "title",
      orderBy: this.$route.query["order"] || "asc",
      activeTab: this.$route.query["tab"] || "articles",
      page: Number.parseInt(this.$route.query["page"]) || 1,
      metas: {},
      results: {},
      pages: {},
      tags: {},
      myColors: "Category10",
      snackMsgWc: null,
      showSnackbar: false,
      searchTagList: []
    };
  },
  created() {
    if (this.$route.query["tags"]) {
      this.searchTagList = this.$route.query["tags"].split("+");
    }
    // fetch the data when the view is created
    // and the data is already being observed
    this.fetchSearchResult();
  },
  watch: {
    // call it again the method if the route changes
    $route: "fetchSearchResult",

    activeTab: newTab => updateSearchURL("tab", newTab),
    searchTagList: function() {
      this.fetchSearchResult();
    }
  },
  computed: {
    articleEmptyStateMsg() {
      let msg = `Sorry, we didn't find any articles matching your search for '${
        this.searchTerm
      }'`;
      if (this.searchTagList.length > 0) {
        msg += "with those tags : " + this.searchTagList.join(",");
      }
      return msg;
    },
    tagList: function() {
      var list = [];
      for (let i = 0; i < this.tags.length; i++) {
        list.push({ name: this.tags[i][0], value: this.tags[i][1] * 1000 });
      }
      return list;
    },

    badgeNumber: function(type) {
      return type => {
        let nb = 0;
        if (type == "articles" && this.metas["articles"] != null) {
          nb = this.metas["articles"]["total_size"];
        }
        if (type == "sotas" && this.metas["sotas"] != null) {
          nb = this.metas["sotas"]["total_size"];
        }
        if (
          (type == "users" || type == "authors") &&
          (this.metas["users"] != null || this.metas["authors"])
        ) {
          const { total_size: authors_size = 0 } = this.metas["authors"];
          const { total_size: users_size = 0 } = this.metas["users"];

          nb = users_size + authors_size;
        }

        return { size: nb };
      };
    },

    isEmptyArticlesTags() {
      return Object.keys(this.articlesTags).length == 0;
    },
    isPaginationVisible(type) {
      return type => {
        return (
          !this.loading &&
          this.metas[type] &&
          this.metas[type]["total_pages"] >= 1
        );
      };
    },
    articlesTitles() {
      if (!this.results["articles"]) {
        return [];
      }

      return this.results["articles"].map(article => ({
        title: article.title,
        reference: article.reference,
        year: article.year,
        domain: article.category
      }));
    },

    articlesTags() {
      if (!this.results["articles"]) {
        return {};
      }
      return this.results["articles"].reduce((acc, article) => {
        // As the same time, push the article title
        article.keywords.forEach(({ name, slug }) => {
          const nb = acc[slug] ? acc[slug]["occur"] : 0;
          acc[slug] = { name, occur: nb + 1 };
        });
        return acc;
      }, {});
    },

    /**
     * Group articles based on common keywords among them.
     */
    relatedArticles() {
      let relatedArticlesList = [];
      const { articles } = this.results;
      if (!articles) {
        return [];
      }
      for (let i = 0; i < articles.length; i++) {
        let keywords = articles[i].keywords;
        for (let j = i + 1; j < articles.length; j++) {
          let commonKeyword = "";
          let commonArticle = [];
          let alreadyIn = false;
          for (let k = 0; k < keywords.length; k++) {
            let keywordName = keywords[k].name;
            for (let l = 0; l < articles[j].keywords.length; l++) {
              if (keywordName === articles[j].keywords[l].name) {
                commonKeyword += keywordName + ", ";
                if (alreadyIn === false) {
                  alreadyIn = true;
                  commonArticle.push(i);
                  commonArticle.push(j);
                }
              }
            }
          }
          commonArticle.push(commonKeyword);
          if (commonArticle.length > 1) {
            relatedArticlesList.push(commonArticle);
          }
        }
      }
      return relatedArticlesList;
    }
  },
  methods: {
    updateSearchURL(type, by) {
      if (type == "tab") {
        this.changingTab = true;
      }
      const query = { ...this.$route.query };
      query[type] = by;
      this.$router.push({ query });
    },
    removeTagFromSearchList(tagName) {
      const index = this.searchTagList.indexOf(tagName);
      if (index < 0) return;
      this.$delete(this.searchTagList, index);
      this.updateSearchURL("tags", this.searchTagList);
    },
    fetchSearchResult() {
      // if (this.changingTab) {
      //   this.changingTab = false;
      //   return;
      // }
      this.loading = true;
      this.searchTerm = this.$route.query["search"];

      const searchQuery = {
        term: this.searchTerm,
        tag: this.searchTagList.join(","),
        sort: this.sortBy,
        order: this.orderBy,
        page: !this.changingTab ? this.page : 1,
        only: !this.changingTab ? this.activeTab : undefined
      };

      //console.log(searchQuery)

      this.fetchTags();

      return getSearchResults(searchQuery)
        .then(res => {
          this.loading = false;
          if (!res["metas"]) {
          }
          Object.keys(res["metas"])
            // .filter(type => res["metas"][type] != null)
            .forEach(type => {
              this.$set(this.metas, type, res["metas"][type]);
              this.$set(this.results, type, res[type]);
            });
        })
        .catch(console.error);
    },
    fetchTags() {
      getTags(this.searchTerm).then(res => {
        this.tags = res;
      });
    },
    wordClickHandler(name, value, vm) {
      this.showSnackbar = true;
      this.snackMsgWc = 'Tag: "' + name + '" Occurence: ' + value / 1000;

      if (!this.searchTagList.includes(name)) {
        this.searchTagList.push(name);
        this.activeTab = "articles";
        this.updateSearchURL("tags", this.searchTagList.join("+"));
      }
    }
  }
};
</script>

<style scoped>
.loading-search-results {
  /* position: relative;
  width: 100%;
  top: 0;
  margin-left: 40px; */
}

.sort-options {
  position: fixed;
  /* top: 0;
  left: 0;
  margin-top: 75px; */
}

.badge {
  width: 19px;
  height: 19px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 2px;
  right: 2px;
  background: #ff5252;
  border-radius: 100%;
  color: #fff;
  font-size: 10px;
  font-style: normal;
  font-weight: 600;
  letter-spacing: -0.05em;
  font-family: "Roboto Mono", monospace;
}

.md-radio {
  display: flex;
}
</style>

