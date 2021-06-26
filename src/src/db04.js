connection = new Mongo()
db = connection.getDB("news");

// List all articles.
db.articles.count();

// All articles authored by Joan E. Solsman, sorted alphabetically.
db.articles.find({author:{$eq:"Joan E. Solsman"}},{_id:0,title:1}).sort({title:1});

// The total number of articles per article source (name), also alphabetically.
//db.articles.find({},{"_id":0,"source":{"name":1}}).sort({KEY:1})
db.articles.aggregate( [
   { $group: { _id: 0, source: { $sum: 1 } } }
   { $project: { _id: 0, source: { name: 1 } } }
] ).sort({title:1});

// All articles having a specific word, programming, (picked from your collection) in their content.
db.articles.find({content: {$in: /^programming^/}},{_id:0, title:1, url:1});

// Custom: All articles published in or after April, sorted by date published.
db.articles.find({publishedAt: {$gt: /2021-04^/}}, {_id: 0, title: 1, source:{name: 1}}).sort({publishedAt:-1});
